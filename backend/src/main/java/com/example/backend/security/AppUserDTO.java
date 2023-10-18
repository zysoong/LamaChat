package com.example.backend.security;

public record AppUserDTO(
        String userId,
        String userName,
        boolean isVirtualAgent
) {
}
