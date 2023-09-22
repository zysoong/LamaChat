package com.example.frontendjavafx.model;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ChatSession{
    private String chatSessionId;
    private String uniqueSessionIdentifier;
    private List<ChatMessage> chat_messages;

    public String chatSessionId(){return this.chatSessionId; }
    public String uniqueSessionIdentifier(){return this.uniqueSessionIdentifier; }
    public List<ChatMessage> chat_messages(){return this.chat_messages; }


}
