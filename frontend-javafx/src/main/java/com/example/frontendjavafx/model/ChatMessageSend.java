package com.example.frontendjavafx.model;

import java.util.Date;

public record ChatMessageSend(

    String senderId,
    String recipientId,
    Date timestamp,
    String content
) {

}
