package com.hotel.controller;

import com.hotel.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // <-- Dùng @RestController vì đây là API
@RequestMapping("/payment")
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 4. Server VNPAY gọi ngầm (IPN) vào đây
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<String> handleIpn(HttpServletRequest request) {
        int result = paymentService.handlePaymentCallback(request);

        if (result == 1) { // Thành công
            return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
        } else if (result == 2) { // Thất bại
            return ResponseEntity.ok("{\"RspCode\":\"99\",\"Message\":\"Confirm Failed\"}");
        } else { // Chữ ký lỗi
            return ResponseEntity.ok("{\"RspCode\":\"97\",\"Message\":\"Invalid Signature\"}");
        }
    }
}