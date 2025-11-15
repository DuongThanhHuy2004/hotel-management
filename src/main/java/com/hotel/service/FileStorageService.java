package com.hotel.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile multipartFile);
    // (Sau này có thể thêm hàm deleteFile)
}