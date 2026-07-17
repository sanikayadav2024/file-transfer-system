package com.example.filetransfer;

import com.example.filetransfer.security.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class BrowserLauncher {

    @Autowired
    private TokenManager tokenManager;

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.ssl.key-store:}")
    private String keyStore;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        new Thread(() -> {
            try {
                // Wait briefly for a safe startup display, then open the browser
                Thread.sleep(1500);

                String ip = InetAddress.getLocalHost().getHostAddress();
                String scheme = (keyStore != null && !keyStore.isEmpty()) ? "https" : "http";
                String url = scheme + "://" + ip + ":" + port + "/?token=" + tokenManager.getToken();

                System.out.println("QuickShare Server is ready!");
                System.out.println("Access local site at: " + url);

                // Open default browser on Windows
                Runtime.getRuntime().exec("cmd /c start " + url.replace("&", "^&"));

            } catch (Exception e) {
                System.err.println("Failed to automatically launch browser: " + e.getMessage());
            }
        }).start();
    }
}
