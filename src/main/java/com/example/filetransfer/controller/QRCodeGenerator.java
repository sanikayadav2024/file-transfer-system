package com.example.filetransfer.controller;
import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.*;

import java.io.File;
import java.nio.file.Path;

public class QRCodeGenerator {
    public static void main(String[] args) throws Exception {
        String url = "http://192.168.43.150:8080"; // your IP

        int width = 300;
        int height = 300;

        BitMatrix matrix = new MultiFormatWriter()
                .encode(url, BarcodeFormat.QR_CODE, width, height);

        Path path = new File("qrcode.png").toPath();
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        System.out.println("QR Code Generated!");
    }
}