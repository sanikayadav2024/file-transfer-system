
package com.example.filetransfer.controller;

import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(UPLOAD_DIR + file.getOriginalFilename());
            file.transferTo(dest);

            return "Uploaded: " + file.getOriginalFilename();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {

        File file = new File(UPLOAD_DIR + filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .body(resource);
    }
    @DeleteMapping("/delete/{filename}")
    public String deleteFile(@PathVariable String filename) {
        try {
            File file = new File(UPLOAD_DIR + filename);

            if (file.exists()) {
                file.delete();
                return "Deleted: " + filename;
            } else {
                return "File not found!";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
