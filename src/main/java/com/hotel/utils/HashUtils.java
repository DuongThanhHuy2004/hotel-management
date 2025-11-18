package com.hotel.utils;

import java.nio.charset.StandardCharsets;

public class HashUtils {

    /**
     * Hàm tiện ích để tạo chữ ký HmacSHA512 (dùng cho VNPAY)
     * @param key Chuỗi bí mật
     * @param data Dữ liệu cần hash (đã sắp xếp)
     * @return Chuỗi chữ ký
     */
    public static String hmacSHA512(String key, String data) {
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
}