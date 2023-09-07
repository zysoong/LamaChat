package com.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Document(collection = "chat_messages")
public record ChatMessage(
    @MongoId
    String messageId,
    String senderId,
    SimpleDateFormat timestamp,
    String content,
    ChatSession chatSession
) {

}
