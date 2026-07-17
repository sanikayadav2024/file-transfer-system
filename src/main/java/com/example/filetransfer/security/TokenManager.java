package com.example.filetransfer.security;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TokenManager {
    private final String token;

    public TokenManager() {
        this.token = UUID.randomUUID().toString();
    }

    public String getToken() {
        return token;
    }

    public boolean isValid(String inputToken) {
        return token.equals(inputToken);
    }
}
