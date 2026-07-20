package com.example.filetransfer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${file.upload-dir:${user.home}/Downloads/}")
    private String uploadDir;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,txt,zip,mp4,mp3,xls,xlsx,ppt,pptx}")
    private String allowedExtensions;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file selected. Please choose a file to upload.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ResponseEntity.badRequest().body("Unsupported file: file has no name.");
        }

        String cleanedFilename = sanitizeFilename(originalFilename);
        if (cleanedFilename == null || !cleanedFilename.contains(".")) {
            return ResponseEntity.badRequest().body("Invalid or unsupported filename.");
        }

        String extension = cleanedFilename.substring(cleanedFilename.lastIndexOf(".") + 1).toLowerCase();

        List<String> allowedExtensionsList =
                Arrays.asList(allowedExtensions.toLowerCase().split(","));

        if (!allowedExtensionsList.contains(extension)) {
            return ResponseEntity.badRequest()
                    .body("Unsupported file type: ." + extension);
        }

        if (file.getSize() > maxFileSize) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large. Maximum allowed size is "
                            + (maxFileSize / (1024 * 1024)) + "MB.");
        }

        try {
            File dir = new File(uploadDir);

            if (!dir.exists() && !dir.mkdirs()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Upload failed: could not create upload directory.");
            }

            File dest = new File(dir, cleanedFilename);
            file.transferTo(dest);

            return ResponseEntity.ok("Uploaded: " + cleanedFilename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed due to a server error.");
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File too large. Maximum allowed size is "
                        + (maxFileSize / (1024 * 1024)) + "MB.");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {

        String cleanedFilename = sanitizeFilename(filename);

        if (cleanedFilename == null) {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(uploadDir, cleanedFilename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource =
                new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .body(resource);
    }

    private String sanitizeFilename(String filename) {

        if (filename == null) {
            return null;
        }

        String cleaned = filename.replace('\\', '/');

        int lastSlash = cleaned.lastIndexOf('/');

        if (lastSlash != -1) {
            cleaned = cleaned.substring(lastSlash + 1);
        }

        if (cleaned.isEmpty()
                || cleaned.equals(".")
                || cleaned.equals("..")
                || cleaned.contains("/")
                || cleaned.contains("\\")
                || cleaned.contains("\u0000")) {
            return null;
        }

        return cleaned;
    }
}