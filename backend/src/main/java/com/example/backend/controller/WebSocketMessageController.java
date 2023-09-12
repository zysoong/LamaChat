package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatMessageReceiveDTO;
import com.example.backend.model.ChatSession;
import com.example.backend.service.ChatSessionService;
import com.example.backend.utilities.SessionIdentifierUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionService chatSessionService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageReceiveDTO chatMessageDto) {

        ChatMessage chatMessageToAdd = new ChatMessage(
                null,
                chatMessageDto.senderId(),
                chatMessageDto.recipientId(),
                chatMessageDto.timestamp(),
                chatMessageDto.content()
        );

        ChatMessage msgStored = chatSessionService
                .addChatMessageToChatSession(
                        chatMessageDto.senderId(),
                        chatMessageDto.recipientId(),
                        chatMessageToAdd
                );

        System.out.println("[WS MSG]: " + msgStored);

        messagingTemplate.convertAndSendToUser(
                chatMessageDto.recipientId(),"/queue/messages",
                msgStored);
    }

}
