package com.hotel.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VnpayConfig {
    @Value("${vnpay.url}")
    private String vnp_Url;

    @Value("${vnpay.return.url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.ipn.url}")
    private String vnp_IpnUrl;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;
}