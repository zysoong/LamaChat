package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Document(collection = "chat_messages")
public record ChatMessage(
    @MongoId
    String messageId,
    String senderId,
    String recipientId,
    Date timestamp,
    String content
) {

}
