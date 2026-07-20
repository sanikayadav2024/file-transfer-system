package com.example.filetransfer.controller;

import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/files")
public class FileController {

    private final String UPLOAD_DIR = System.getProperty("user.home")
            + File.separator + "Downloads" + File.separator;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx",
            "txt", "zip", "mp4", "mp3", "xls", "xlsx", "ppt", "pptx");

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Set<String> RESERVED_FILENAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5",
            "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5",
            "LPT6", "LPT7", "LPT8", "LPT9"
    );

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

        String extension = cleanedFilename.substring(cleanedFilename.lastIndexOf('.') + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body("Unsupported file type: ." + extension);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large. Maximum allowed size is "
                            + (MAX_FILE_SIZE / (1024 * 1024)) + "MB.");
        }

        try {

            File dir = new File(UPLOAD_DIR);

            if (!dir.exists() && !dir.mkdirs()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Upload failed: could not create upload directory.");
            }

            File dest = new File(UPLOAD_DIR + cleanedFilename);

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
                        + (MAX_FILE_SIZE / (1024 * 1024)) + "MB.");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {

        String cleanedFilename = sanitizeFilename(filename);

        if (cleanedFilename == null) {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(UPLOAD_DIR + cleanedFilename);

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

        // Normalize path separators
        String cleaned = filename.replace('\\', '/');

        // Remove path information
        int lastSlash = cleaned.lastIndexOf('/');

        if (lastSlash != -1) {
            cleaned = cleaned.substring(lastSlash + 1);
        }

        cleaned = cleaned.trim();

        if (cleaned.isEmpty()) {
            return null;
        }

        // Prevent overly long filenames
        if (cleaned.length() > MAX_FILENAME_LENGTH) {
            return null;
        }

        // Prevent "." and ".."
        if (cleaned.equals(".") || cleaned.equals("..")) {
            return null;
        }

        // Prevent path traversal and null bytes
        if (cleaned.contains("/")
                || cleaned.contains("\\")
                || cleaned.contains("\u0000")) {
            return null;
        }

        // Reject reserved Windows filenames
        String baseName = cleaned;

        int dotIndex = cleaned.indexOf('.');

        if (dotIndex != -1) {
            baseName = cleaned.substring(0, dotIndex);
        }

        if (RESERVED_FILENAMES.contains(baseName.toUpperCase())) {
            return null;
        }

        // Allow only safe characters
        if (!cleaned.matches("[A-Za-z0-9._()\\- ]+")) {
            return null;
        }

        return cleaned;
    }
}