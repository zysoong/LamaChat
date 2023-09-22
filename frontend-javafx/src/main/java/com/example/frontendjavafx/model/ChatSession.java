package com.example.frontendjavafx.model;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

public record ChatSession(
        String chatSessionId,
        String uniqueSessionIdentifier,
        List<ChatMessage> chat_messages
) {
}
