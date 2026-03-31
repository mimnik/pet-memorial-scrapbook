package com.pet.memorial.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String storeImage(MultipartFile file) {
        validateFile(file);
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/WebP 图片上传");
        }
        return storeFile(file, ".jpg");
    }

    public String storeMedia(MultipartFile file) {
        validateFile(file);
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_VIDEO_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/WebP 图片或 MP4/WebM/MOV 视频上传");
        }
        String fallbackExtension = ALLOWED_VIDEO_TYPES.contains(contentType) ? ".mp4" : ".jpg";
        return storeFile(file, fallbackExtension);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        long maxBytes = 1024L * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("单个文件大小不能超过 1024MB");
        }
    }

    private String storeFile(MultipartFile file, String fallbackExtension) {
        String extension = resolveExtension(file.getOriginalFilename(), fallbackExtension);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("文件保存失败", ex);
        }

        return fileName;
    }

    private String resolveExtension(String originalName, String fallbackExtension) {
        String filename = StringUtils.hasText(originalName) ? originalName : "";
        int index = filename.lastIndexOf('.');
        return index > -1 ? filename.substring(index).toLowerCase() : fallbackExtension;
    }
}
