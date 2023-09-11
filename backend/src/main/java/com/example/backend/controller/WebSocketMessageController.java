package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
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
    public void processMessage(@Payload ChatMessage chatMessage) {

        /*
        var chatId = chatRoomService
                .getChatId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true);
        chatMessage.setChatId(chatId.get());

        chatSessionService.addChatMessageToChatSession(
                chatMessage.senderId(),
                SessionIdentifierUtilities.getReceiverFromUniqueIdentifier(
                        chatMessage.chatSession().uniqueSessionIdentifier(),
                        chatMessage.senderId()
                ),
                chatMessage
        );

        ChatMessage saved = chatMessageService.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),"/queue/messages",
                new ChatNotification(
                        saved.getId(),
                        saved.getSenderId(),
                        saved.getSenderName()));*/
    }

}
