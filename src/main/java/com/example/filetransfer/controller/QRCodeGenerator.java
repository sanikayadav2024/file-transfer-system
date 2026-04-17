package com.example.filetransfer.controller;
import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.*;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QRCodeGenerator {
    public QRCodeGenerator() throws Exception {
        String url = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String IP =  ip.getHostAddress();
            url = "http://" + IP + ":8080";
        } catch (Exception e) {
            e.printStackTrace();
        }

        int width = 300;
        int height = 300;

        BitMatrix matrix = new MultiFormatWriter()
                .encode(url, BarcodeFormat.QR_CODE, width, height);
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "qrcode.png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        System.out.println("QR Code Generated!");
    }
}