package com.example.filetransfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String uploadDir =
            System.getProperty("user.home") + File.separator + "Downloads" + File.separator;

    @Test
    void testSuccessfulUpload() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded: sample.txt"));

        Files.deleteIfExists(Path.of(uploadDir + "sample.txt"));
    }

    @Test
    void testInvalidFileType() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "virus.exe",
                "application/octet-stream",
                "abc".getBytes()
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported file type: .exe"));
    }

    @Test
    void testOversizedFileUpload() throws Exception {

        byte[] data = new byte[11 * 1024 * 1024];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                data
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void testEmptyUpload() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadMissingFile() throws Exception {

        mockMvc.perform(get("/files/download/notfound.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPathTraversalProtection() throws Exception {

        mockMvc.perform(get("/files/download/../../secret.txt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSuccessfulDownload() throws Exception {

        Path path = Path.of(uploadDir + "download.txt");

        Files.write(path, "Download Test".getBytes());

        mockMvc.perform(get("/files/download/download.txt"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        Files.deleteIfExists(path);
    }
}