package com.example.filetransfer.controller;

import com.example.filetransfer.security.TokenManager;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

@RestController
public class QRCodeGenerator {

    @Autowired
    private TokenManager tokenManager;

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.ssl.key-store:}")
    private String keyStore;

    @GetMapping(value = "/api/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() {
        try {
            // 1. Fetch current local IP address dynamically
            InetAddress ip = InetAddress.getLocalHost();
            String hostIP = ip.getHostAddress();

            // 2. Build URL with correct scheme, port, and security token
            String scheme = (keyStore != null && !keyStore.isEmpty()) ? "https" : "http";
            String url = scheme + "://" + hostIP + ":" + port + "/?token=" + tokenManager.getToken();

            int width = 300;
            int height = 300;

            // 3. Generate QR code bit matrix
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(url, BarcodeFormat.QR_CODE, width, height);

            // 4. Write image directly into an in-memory byte array stream instead of saving a file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            // 5. Return the image bytes with the correct HTTP headers
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
