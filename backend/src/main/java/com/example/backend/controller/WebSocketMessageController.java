package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatMessageFlaskDTO;
import com.example.backend.model.ChatMessageReceiveDTO;
import com.example.backend.model.FlaskResponseDTO;
import com.example.backend.security.AppUser;
import com.example.backend.service.ChatSessionService;
import com.example.backend.security.AppUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Controller
public class WebSocketMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private AppUserService appUserService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageReceiveDTO chatMessageDto) {

        ChatMessage msgStored = saveMessage(chatMessageDto);
        System.out.println("[WS MSG]: " + msgStored);

        if (appUserService
                .findAppUserByUserId(chatMessageDto.recipientId())
                .isVirtualAgent()
        )
        {
            checkAndProcessForVirtualAgent(chatMessageDto);
        }

        messagingTemplate.convertAndSendToUser(
                chatMessageDto.recipientId(),"/queue/messages",
                msgStored);
    }

    public ChatMessage saveMessage(ChatMessageReceiveDTO chatMessageDto){
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

        return msgStored;
    }

    public void checkAndProcessForVirtualAgent(ChatMessageReceiveDTO chatMessageDto)
    {

        try {

            AppUser recipientUser = appUserService.findAppUserByUserId(chatMessageDto.recipientId());
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();

            ChatMessageFlaskDTO flaskDTO = new ChatMessageFlaskDTO(chatMessageDto.senderId(), chatMessageDto.content());
            String requestBody = objectMapper.writeValueAsString(flaskDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(recipientUser.api()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(responseBody -> mapToFlaskResponse(responseBody))
                    .thenApply((flaskResponseDto) -> {
                        LocalDate localDate = LocalDate.now();
                        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        return new ChatMessage(
                                null,
                                chatMessageDto.recipientId(),
                                chatMessageDto.senderId(),
                                date, flaskResponseDto.content());
                    })
                    .thenApply((replyChatMessage) -> {
                        return chatSessionService.addChatMessageToChatSession(
                                replyChatMessage.senderId(),
                                replyChatMessage.recipientId(),
                                replyChatMessage
                        );
                    })
                    .thenApply((replyMsgStored) -> {
                        messagingTemplate.convertAndSendToUser(
                                replyMsgStored.recipientId(),"/queue/messages",
                                replyMsgStored);
                        return null;
                    });
        }
        catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    private FlaskResponseDTO mapToFlaskResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, FlaskResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



}
