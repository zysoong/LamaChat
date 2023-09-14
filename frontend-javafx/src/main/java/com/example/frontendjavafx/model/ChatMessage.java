package com.example.frontendjavafx.model;

import java.util.Date;

public record ChatMessage(

    String messageId,
    String senderId,
    String recipientId,
    Date timestamp,
    String content
) {

}
