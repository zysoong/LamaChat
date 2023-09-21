package com.example.frontendjavafx.model;

import java.util.List;


public record ChatSession(
    String chatSessionId,
    String uniqueSessionIdentifier,
    List<ChatMessage> chat_messages
) {
}
