package com.example.frontendjavafx.security;

public record AppUserDTO(
        String userId,
        String userName,
        boolean isVirtualAgent
) {
}
