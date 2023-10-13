package com.example.frontendjavafx.security;

import com.example.frontendjavafx.model.ChatSession;

import java.util.List;

public record AppUserIdAndNameDTO(
        String userId,
        String userName
) {
}
