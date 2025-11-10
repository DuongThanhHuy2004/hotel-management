package com.hotel.controller;

import com.hotel.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 1. Client bấm nút "Thanh toán", gọi hàm này
    @GetMapping("/create-payment/{bookingId}")
    public String createPayment(@PathVariable Long bookingId, HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createPaymentUrl(bookingId, request);
            return "redirect:" + paymentUrl; // 2. Chuyển hướng sang VNPAY
        } catch (Exception e) {
            return "redirect:/my-bookings?error=" + e.getMessage();
        }
    }

    // 3. VNPAY chuyển hướng trình duyệt của Client về đây
    @GetMapping("/vnpay-callback")
    public String paymentCallback(HttpServletRequest request, Model model) {
        int result = paymentService.handlePaymentCallback(request);

        if (result == 1) { // Thành công
            return "client/payment-success"; // (Sẽ tạo ở bước 6)
        } else if (result == 2) { // Thất bại
            return "client/payment-fail"; // (Sẽ tạo ở bước 6)
        } else { // Chữ ký không hợp lệ
            return "client/payment-fail";
        }
    }
}