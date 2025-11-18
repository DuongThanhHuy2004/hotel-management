package com.hotel.service;

import com.hotel.config.VnpayConfig;
import com.hotel.entity.Booking;
import com.hotel.entity.Payment;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.utils.HashUtils;
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
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(VnpayConfig vnpayConfig, BookingRepository bookingRepository,
                              BookingService bookingService, PaymentRepository paymentRepository) {
        this.vnpayConfig = vnpayConfig;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.paymentRepository = paymentRepository;
    }

    // (Hàm private hmacSHA512 ĐÃ BỊ XÓA, vì đã chuyển sang HashUtils)

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

        String vnp_IpAddr = "13.114.220.106"; // Dùng IP test cứng
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

        // ===================================
        // SỬA LỖI Ở ĐÂY (Gọi HashUtils)
        // ===================================
        String vnp_SecureHash = HashUtils.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());

        query.append("&vnp_SecureHash=");
        query.append(vnp_SecureHash);

        return vnpayConfig.getVnp_Url() + "?" + query;
    }

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

        // ===================================
        // SỬA LỖI Ở ĐÂY (Gọi HashUtils)
        // ===================================
        String calculatedHash = HashUtils.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());

        if (calculatedHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String vnp_TxnRef = fields.get("vnp_TxnRef");
            Long bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);
            double amount = Double.parseDouble(fields.get("vnp_Amount")) / 100;

            Booking booking = bookingService.findById(bookingId);

            // Ghi nhật ký Payment
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