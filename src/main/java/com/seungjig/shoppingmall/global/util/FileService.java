package com.seungjig.shoppingmall.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Component
public class FileService {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String fileUrl) {
        return Paths.get(fileDir + fileUrl).toString();
    }

    // 파일 저장
    public String storeFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFileName);
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return storeFileName;
    }

    //파일 삭제
    public boolean deleteFile(String storeFileName) {
        if (storeFileName == null || storeFileName.isEmpty()) {
            return false;
        }

        String fullPath = getFullPath(storeFileName);
        Path path = Paths.get(fullPath);

        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int pos = originalFilename.lastIndexOf(".");
        if (pos == -1) {
            return "";
        }
        return originalFilename.substring(pos + 1);
    }

    public boolean isImageFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return false;
        }

        String contentType = multipartFile.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }

        String ext = extractExt(multipartFile.getOriginalFilename()).toLowerCase();
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(ext);
    }
}
