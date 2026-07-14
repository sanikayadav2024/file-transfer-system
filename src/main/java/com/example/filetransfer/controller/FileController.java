package com.example.filetransfer.controller;

import com.example.filetransfer.FileTransferApplication;
import com.example.filetransfer.model.FileMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + 
            "Downloads" + File.separator + "QuickShare" + File.separator;

    private final Map<String, FileMetadata> metadataMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File metadataFile = new File(UPLOAD_DIR + "metadata.json");
        if (metadataFile.exists()) {
            try {
                TypeReference<ConcurrentHashMap<String, FileMetadata>> typeRef = new TypeReference<>() {};
                Map<String, FileMetadata> loaded = objectMapper.readValue(metadataFile, typeRef);
                metadataMap.putAll(loaded);

                // Clean up any files that expired while the server was offline
                long now = System.currentTimeMillis();
                boolean changed = false;
                Iterator<Map.Entry<String, FileMetadata>> it = metadataMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, FileMetadata> entry = it.next();
                    FileMetadata meta = entry.getValue();
                    if (meta.getExpirationTime() < now) {
                        File file = new File(UPLOAD_DIR + meta.getFilename());
                        if (file.exists()) {
                            file.delete();
                        }
                        it.remove();
                        changed = true;
                    }
                }
                if (changed) {
                    saveMetadata();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void saveMetadata() {
        try {
            File metadataFile = new File(UPLOAD_DIR + "metadata.json");
            objectMapper.writeValue(metadataFile, metadataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        // Allow localhost to bypass PIN verification
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr)) {
            return true;
        }

        // Check header
        String pin = request.getHeader("X-QuickShare-PIN");
        if (pin == null || pin.isEmpty()) {
            // Check parameter
            pin = request.getParameter("pin");
        }
        return FileTransferApplication.SECURITY_PIN.equals(pin);
    }

    @GetMapping("/api/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        String remoteAddr = request.getRemoteAddr();
        boolean isLocal = "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr);

        try {
            info.put("isLocalhost", isLocal);
            info.put("serverIp", InetAddress.getLocalHost().getHostAddress());
            info.put("serverPort", 8080);
            if (isLocal) {
                info.put("securityPin", FileTransferApplication.SECURITY_PIN);
            }
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/api/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPin(@RequestBody Map<String, String> body) {
        String pin = body.get("pin");
        Map<String, Object> response = new HashMap<>();
        if (FileTransferApplication.SECURITY_PIN.equals(pin)) {
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // CRUD: CREATE
    @PostMapping("/files/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid security PIN");
        }

        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }

            // Secure filename to prevent path traversal
            String safeName = new File(originalName).getName();
            String uniqueName = UUID.randomUUID().toString() + "_" + safeName;
            
            File dest = new File(UPLOAD_DIR + uniqueName);
            file.transferTo(dest);

            long size = file.getSize();
            long now = System.currentTimeMillis();
            long expiration = now + 3600000; // 1 hour expiration

            String finalTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : safeName;
            String finalDesc = (description != null) ? description.trim() : "";

            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            FileMetadata metadata = new FileMetadata(
                    uniqueName,
                    safeName,
                    finalTitle,
                    finalDesc,
                    size,
                    now,
                    expiration,
                    contentType
            );

            metadataMap.put(uniqueName, metadata);
            saveMetadata();

            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // CRUD: READ (List)
    @GetMapping("/files/list")
    public ResponseEntity<?> listFiles(HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid security PIN");
        }

        long now = System.currentTimeMillis();
        List<FileMetadata> activeFiles = metadataMap.values().stream()
                .filter(meta -> meta.getExpirationTime() > now)
                .sorted(Comparator.comparingLong(FileMetadata::getUploadTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeFiles);
    }

    // CRUD: READ (Download)
    @GetMapping("/files/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) throws IOException {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileMetadata meta = metadataMap.get(filename);
        if (meta == null || meta.isExpired()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(UPLOAD_DIR + filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    // CRUD: UPDATE
    @PutMapping("/files/update/{filename}")
    public ResponseEntity<?> updateFile(
            @PathVariable String filename,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        FileMetadata meta = metadataMap.get(filename);
        if (meta == null || meta.isExpired()) {
            return ResponseEntity.notFound().build();
        }

        if (body.containsKey("title")) {
            meta.setTitle(body.get("title"));
        }
        if (body.containsKey("description")) {
            meta.setDescription(body.get("description"));
        }

        saveMetadata();
        return ResponseEntity.ok(meta);
    }

    // CRUD: DELETE
    @DeleteMapping("/files/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        FileMetadata meta = metadataMap.remove(filename);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(UPLOAD_DIR + filename);
        if (file.exists()) {
            file.delete();
        }

        saveMetadata();
        return ResponseEntity.ok("Deleted: " + meta.getOriginalName());
    }

    // Scheduled background task for auto-cleanup
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredFiles() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        Iterator<Map.Entry<String, FileMetadata>> iterator = metadataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, FileMetadata> entry = iterator.next();
            FileMetadata meta = entry.getValue();
            if (meta.getExpirationTime() < now) {
                File file = new File(UPLOAD_DIR + meta.getFilename());
                if (file.exists()) {
                    file.delete();
                }
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            saveMetadata();
        }
    }
}