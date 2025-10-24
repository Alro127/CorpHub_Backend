package com.example.ticket_helpdesk_backend.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    public String uploadFile(String bucket, MultipartFile file, String prefix) {
        try {
            String objectName = (prefix != null ? prefix + "/" : "")
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();
// Chỗ tên file này cần lưu ý lại
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return objectName; // trả về key, để DB lưu
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }
    public String uploadFile(String bucket, InputStream inputStream, String fileName, String prefix) {
        try {
            String objectName = (prefix != null ? prefix + "/" : "")
                    + UUID.randomUUID() + "_" + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(Files.probeContentType(Path.of(fileName)))
                            .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    public InputStream downloadFile(String bucket, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

    public void deleteFile(String bucket, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from MinIO", e);
        }
    }

    public String getPresignedUrl(String bucket, String objectName) {
        try {
            return null;
//            return minioClient.getPresignedObjectUrl(
//                    GetPresignedObjectUrlArgs.builder()
//                            .bucket(bucket)
//                            .object(objectName)
//                            .method(Method.GET)
//                            .expiry(7, TimeUnit.DAYS)
//                            .build()
//            );
        } catch (Exception e) {
            System.err.println("❌ Error generating presigned URL: " + e.getMessage());
            e.printStackTrace();
            return null; // Chỉ trả về null khi có lỗi
        }
    }

}
