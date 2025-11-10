package com.hotel.service;

import com.hotel.config.VnpayConfig;
import com.hotel.entity.Booking;
import com.hotel.entity.Payment; // Thêm import
import com.hotel.repository.BookingRepository;
import com.hotel.repository.PaymentRepository; // Thêm import
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final VnpayConfig vnpayConfig;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final PaymentRepository paymentRepository; // Thêm repository

    // Sửa lại constructor
    public PaymentServiceImpl(VnpayConfig vnpayConfig, BookingRepository bookingRepository,
                              BookingService bookingService, PaymentRepository paymentRepository) {
        this.vnpayConfig = vnpayConfig;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.paymentRepository = paymentRepository; // Thêm vào
    }

    // (Hàm hmacSHA512 giữ nguyên)
    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMACSHA512 signature", e);
        }
    }

    // (Hàm getIpAddress giữ nguyên)
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


    @Override
    public String createPaymentUrl(Long bookingId, HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not in PENDING state");
        }

        long amount = (long) (booking.getTotalPrice() * 100);
        String vnp_TxnRef = bookingId.toString() + "_" + System.currentTimeMillis();

        // Dùng IP test cứng cho an toàn
        String vnp_IpAddr = "13.114.220.106";
        String vnp_OrderInfo = "Payment for booking #" + bookingId;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                query.append('&');
                hashData.append('&');
            }
        }

        // Xóa dấu & cuối cùng
        query.deleteCharAt(query.length() - 1);
        hashData.deleteCharAt(hashData.length() - 1);

        String vnp_SecureHash = hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=");
        query.append(vnp_SecureHash);

        return vnpayConfig.getVnp_Url() + "?" + query;
    }

    // SỬA LẠI HÀM NÀY ĐỂ GHI LOG PAYMENT
    @Override
    public int handlePaymentCallback(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        if (vnp_SecureHash == null) return 0;

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            hashData.append(fieldName);
            hashData.append('=');
            hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            hashData.append('&');
        }
        hashData.deleteCharAt(hashData.length() - 1);

        String calculatedHash = hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());

        if (calculatedHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String vnp_TxnRef = fields.get("vnp_TxnRef");
            Long bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);
            double amount = Double.parseDouble(fields.get("vnp_Amount")) / 100;

            // Lấy booking
            Booking booking = bookingService.findById(bookingId);

            // ===========================================
            // (PHẦN MỚI) TẠO BẢN GHI THANH TOÁN (PAYMENT)
            // ===========================================
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(amount);
            payment.setTransactionCode(vnp_TxnRef);
            payment.setPaymentMethod("VNPAY");

            if ("00".equals(vnp_ResponseCode)) {
                // Giao dịch thành công
                if ("PENDING".equals(booking.getStatus())) {
                    bookingService.confirmBooking(bookingId);
                }
                payment.setStatus("SUCCESS");
                paymentRepository.save(payment); // Lưu thanh toán
                return 1; // Thành công
            } else {
                // Giao dịch thất bại
                bookingService.cancelBooking(bookingId);
                payment.setStatus("FAILED");
                paymentRepository.save(payment); // Vẫn lưu thanh toán (thất bại)
                return 2; // Thất bại
            }
        } else {
            return 0; // Chữ ký không hợp lệ
        }
    }
}