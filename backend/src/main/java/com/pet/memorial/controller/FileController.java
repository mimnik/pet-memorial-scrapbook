package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeMedia(file);
        String url = "/uploads/" + Objects.requireNonNull(fileName);

        String contentType = file.getContentType();
        String mediaType = contentType != null && contentType.startsWith("video/")
            ? "video"
            : "image";
        return ApiResponse.success("上传成功", Map.of("url", url, "mediaType", mediaType));
    }

    @PostMapping("/upload-image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeImage(file);
        String url = "/uploads/" + Objects.requireNonNull(fileName);
        return ApiResponse.success("图片上传成功", Map.of("url", url, "mediaType", "image"));
    }
}
