package com.example.filetransfer.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator + "Downloads" + File.separator;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx",
            "txt", "zip", "mp4", "mp3", "xls", "xlsx", "ppt", "pptx"
    );

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam(value = "file", required = false) MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "No file selected. Please choose a file to upload.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            throw new IllegalArgumentException(
                    "Unsupported file: file has no name.");
        }

        String cleanedFilename = sanitizeFilename(originalFilename);

        if (cleanedFilename == null || !cleanedFilename.contains(".")) {
            throw new IllegalArgumentException(
                    "Invalid or unsupported filename.");
        }

        String extension =
                cleanedFilename.substring(cleanedFilename.lastIndexOf('.') + 1)
                        .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: ." + extension);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File too large. Maximum allowed size is "
                            + (MAX_FILE_SIZE / (1024 * 1024))
                            + "MB.");
        }

        File dir = new File(UPLOAD_DIR);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create upload directory.");
        }

        File dest = new File(UPLOAD_DIR + cleanedFilename);

        file.transferTo(dest);

        return ResponseEntity.ok("Uploaded: " + cleanedFilename);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String filename)
            throws IOException {

        String cleanedFilename = sanitizeFilename(filename);

        if (cleanedFilename == null) {
            throw new IllegalArgumentException("Invalid filename.");
        }

        File file = new File(UPLOAD_DIR + cleanedFilename);

        if (!file.exists()) {
            throw new FileNotFoundException("Requested file does not exist.");
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