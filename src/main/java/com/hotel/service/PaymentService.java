package com.hotel.service;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createPaymentUrl(Long bookingId, HttpServletRequest request);
    // (Chúng ta cũng sẽ cần một hàm để xử lý kết quả IPN)
    int handlePaymentCallback(HttpServletRequest request);
}