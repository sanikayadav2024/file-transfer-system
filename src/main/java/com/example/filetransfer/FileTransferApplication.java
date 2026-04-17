
package com.example.filetransfer;

import com.example.filetransfer.controller.QRCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class FileTransferApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(FileTransferApplication.class, args);
        QRCodeGenerator qr = new QRCodeGenerator();
        new Thread(() -> {
            try {
                Thread.sleep(3000); // wait for server to start

                String ip = java.net.InetAddress.getLocalHost().getHostAddress();
                String url = "http://" + ip + ":8080";

                System.out.println("Opening: " + url);

                // ✅ MOST RELIABLE (works in EXE)
                Runtime.getRuntime().exec("cmd /c start " + url);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}