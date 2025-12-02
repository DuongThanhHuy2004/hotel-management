package com.hotel.service;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createPaymentUrl(Long bookingId, HttpServletRequest request);
    int handlePaymentCallback(HttpServletRequest request);
}