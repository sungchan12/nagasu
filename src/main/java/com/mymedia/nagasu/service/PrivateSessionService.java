package com.mymedia.nagasu.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrivateSessionService {

    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    public String createSession() {
        var token = UUID.randomUUID().toString();
        activeSessions.add(token);
        return token;
    }

    public boolean isValidSession(String token) {
        return token != null && activeSessions.contains(token);
    }

    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}