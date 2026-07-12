package com.example.filetransfer.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

@RestController
public class QRCodeGenerator{

    @GetMapping(value = "/api/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() {
        try {
            // 1. Fetch current local IP address dynamically
            InetAddress ip = InetAddress.getLocalHost();
            String hostIP = ip.getHostAddress();
            String url = "http://" + hostIP + ":8080";

            int width = 300;
            int height = 300;

            // 2. Generate QR code bit matrix
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(url, BarcodeFormat.QR_CODE, width, height);

            // 3. Write image directly into an in-memory byte array stream instead of saving a file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            // 4. Return the image bytes with the correct HTTP headers
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
