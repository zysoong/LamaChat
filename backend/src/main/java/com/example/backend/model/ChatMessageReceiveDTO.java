package com.example.backend.model;

import java.util.Date;

public record ChatMessageReceiveDTO(
        String senderId,
        String recipientId,
        Date timestamp,
        String content
) {
}
