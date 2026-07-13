
package com.example.filetransfer;

import com.example.filetransfer.controller.QRCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
@EnableScheduling
public class FileTransferApplication {

    public static final String SECURITY_PIN = String.format("%04d", (int) (Math.random() * 10000));

    public static void main(String[] args) throws Exception {
        SpringApplication.run(FileTransferApplication.class, args);
        
        new Thread(() -> {
            try {
                Thread.sleep(3000); // wait for server to start

                String ip = java.net.InetAddress.getLocalHost().getHostAddress();
                String url = "http://" + ip + ":8080";

                System.out.println("\n=================================================");
                System.out.println("⚡ QuickDrop Server Running!");
                System.out.println("👉 Host URL: http://localhost:8080");
                System.out.println("👉 Remote URL: " + url);
                System.out.println("🔒 Security PIN for remote devices: " + SECURITY_PIN);
                System.out.println("=================================================\n");

                Runtime.getRuntime().exec("cmd /c start " + url);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}