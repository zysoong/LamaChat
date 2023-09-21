package com.example.frontendjavafx.security;

import com.example.frontendjavafx.model.ChatSession;

import java.util.List;

public record AppUser(
        String userId,

        String userName,
        String password,
        AppUserRole role,
        List<ChatSession> chat_sessions,
        boolean isVirtualAgent,
        String api,
        String apiKey
) {
}
