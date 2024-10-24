package com.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "http://127.0.0.1:5500", maxAge = 3600, methods = {RequestMethod.GET, RequestMethod.POST}, allowedHeaders = "Content-Type")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";
    private final Map<String, UploadInfo> uploadInfoMap = new HashMap<>();

    @PostMapping("/initUpload")
    public ResponseEntity<Map<String, String>> initUpload(@RequestBody Map<String, Object> payload) {
        String filePath = (String) payload.get("filePath");
        long fileSize = Long.parseLong(payload.get("fileSize").toString());
        String uploadId = UUID.randomUUID().toString();
        uploadInfoMap.put(uploadId, new UploadInfo(filePath, fileSize));
        Map<String, String> response = new HashMap<>();
        response.put("uploadId", uploadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/uploadedChunks")
    public ResponseEntity<Map<String, Integer>> getUploadedChunks(@RequestParam String uploadId) {
        UploadInfo info = uploadInfoMap.get(uploadId);
        Map<String, Integer> response = new HashMap<>();
        response.put("uploadedChunks", info != null ? info.getUploadedChunks() : 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadChunk")
    public ResponseEntity<Void> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("filePath") String filePath) throws IOException {

        UploadInfo info = uploadInfoMap.get(uploadId);
        if (info == null) {
            return ResponseEntity.badRequest().build();
        }

        Path uploadPath = Paths.get(UPLOAD_DIR, uploadId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path chunkPath = uploadPath.resolve(String.valueOf(chunkIndex));
        Files.write(chunkPath, chunk.getBytes());

        info.incrementUploadedChunks();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/completeUpload")
    public ResponseEntity<Void> completeUpload(@RequestBody Map<String, String> payload) throws IOException {
        String uploadId = payload.get("uploadId");
        String filePath = payload.get("filePath");

        UploadInfo info = uploadInfoMap.get(uploadId);
        if (info == null) {
            return ResponseEntity.badRequest().build();
        }

        Path uploadPath = Paths.get(UPLOAD_DIR, uploadId);
        Path targetPath = Paths.get(UPLOAD_DIR, filePath);
        Files.createDirectories(targetPath.getParent());

        // 合并所有的块文件
        try {
            Files.createFile(targetPath);
            for (int i = 0; i < info.getUploadedChunks(); i++) {
                Path chunkPath = uploadPath.resolve(String.valueOf(i));
                Files.write(targetPath, Files.readAllBytes(chunkPath), java.nio.file.StandardOpenOption.APPEND);
                Files.delete(chunkPath);
            }
        } finally {
            // 清理临时文件和上传信息
            Files.delete(uploadPath);
            uploadInfoMap.remove(uploadId);
        }

        return ResponseEntity.ok().build();
    }

    private static class UploadInfo {
        private final String filePath;
        private final long fileSize;
        private int uploadedChunks;

        public UploadInfo(String filePath, long fileSize) {
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.uploadedChunks = 0;
        }

        public int getUploadedChunks() {
            return uploadedChunks;
        }

        public void incrementUploadedChunks() {
            uploadedChunks++;
        }

        public String getFilePath() {
            return filePath;
        }

        public long getFileSize() {
            return fileSize;
        }
    }
}
