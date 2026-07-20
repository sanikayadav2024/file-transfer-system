package com.example.filetransfer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileControllerUnitTest {

    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController();
    }

    @Test
    void testUploadWithNullFile() {

        ResponseEntity<String> response = controller.uploadFile(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "No file selected. Please choose a file to upload.",
                response.getBody()
        );
    }

    @Test
    void testUploadEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "",
                        "text/plain",
                        new byte[0]);

        ResponseEntity<String> response = controller.uploadFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testInvalidExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "virus.exe",
                        "application/octet-stream",
                        "abc".getBytes());

        ResponseEntity<String> response = controller.uploadFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Unsupported file type"));
    }

    @Test
    void testOversizedUpload() {

        byte[] data = new byte[11 * 1024 * 1024];

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "large.pdf",
                        "application/pdf",
                        data);

        ResponseEntity<String> response = controller.uploadFile(file);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
    }

    @Test
    void testUploadWithoutFilename() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        null,
                        "text/plain",
                        "hello".getBytes());

        ResponseEntity<String> response = controller.uploadFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}