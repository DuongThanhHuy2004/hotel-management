package com.hotel.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile multipartFile) {
        try {
            // 1. Chuyển MultipartFile thành File
            File file = convertMultiPartFileToFile(multipartFile);

            // 2. Upload lên Cloudinary
            // (Không cần tạo tên file duy nhất, Cloudinary tự làm)
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

            // 3. Xóa file tạm
            file.delete();

            // 4. Trả về URL an toàn (https)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Cloudinary", e);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}