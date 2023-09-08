package com.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
public record ChatMessage(
    @MongoId
    String messageId,
    String senderId,
    LocalDateTime timestamp,
    String content,
    ChatSession chatSession
) {

}
