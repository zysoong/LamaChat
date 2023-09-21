package com.example.backend.security;

public record AppUserRequest(
        String userName,
        String password,
        boolean isVirtualAgent,
        String api,
        String apiKey
)
{
}
