package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatMessageReceiveDTO;
import com.example.backend.model.ChatMessageSendDTO;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.security.AppUser;
import com.example.backend.security.AppUserRepository;
import com.example.backend.security.AppUserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.backend.utilities.SessionIdentifierUtilities.generateSessionUniqueIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class WebSocketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @LocalServerPort
    private int port;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DirtiesContext
    @WithMockUser
    void chatWithRealPerson_thenCreateChatSessionAndAddMessage() throws Exception {

        AppUser user1 = new AppUser(
                null,
                "user1",
                "123456",
                AppUserRole.USER,
                new ArrayList<>(),
                false,
                "", ""
        );

        AppUser user2 = new AppUser(
                null,
                "user2",
                "123456",
                AppUserRole.USER,
                new ArrayList<>(),
                false,
                "", ""
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn();

        HttpServletRequest request = mvcResult.getRequest();
        String requestUrl = request.getRequestURL().toString();

        AppUser savedUser1 =
                appUserRepository
                        .findAppUserByUserName("user1")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName("user2")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        String serverUrl =   "http://localhost:" + port + "/ws";
        System.out.println("SERVERURL: " + serverUrl);

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        StompSessionHandler sessionHandler = new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {}

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {}
        };

        StompSession stompSession = stompClient
                .connectAsync(serverUrl, sessionHandler).join();

        LocalDate localDate = LocalDate.now();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String content = "Hi! How is it going?";

        ChatMessageReceiveDTO sendMsg = new ChatMessageReceiveDTO(
                savedUser1.userId(),
                savedUser2.userId(),
                date,
                content
        );

        byte[] payLoad = objectMapper.writeValueAsBytes(sendMsg);

        stompSession
                .send("/app/chat", payLoad)
                .addReceiptLostTask(
                        new Runnable() {
                            @Override
                            public void run() {

                                AppUser user1AfterCreation =
                                        appUserRepository
                                                .findAppUserByUserName("user1")
                                                .orElseThrow(() ->
                                                        new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                                                );

                                AppUser user2AfterCreation =
                                        appUserRepository
                                                .findAppUserByUserName("user2")
                                                .orElseThrow(() ->
                                                        new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                                                );

                                System.out.println(user1AfterCreation);

                                String uniqueSessionId = generateSessionUniqueIdentifier(savedUser1.userId(), savedUser2.userId());

                                assertEquals(user1AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                                        uniqueSessionId);

                                assertEquals(user2AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                                        uniqueSessionId);

                                assertEquals(user1AfterCreation.chat_sessions().get(0).chat_messages().get(0).content(),
                                        content);

                                assertEquals(user2AfterCreation.chat_sessions().get(0).chat_messages().get(0).content(),
                                        content);

                                chatSessionRepository
                                        .findChatSessionByUniqueSessionIdentifier(uniqueSessionId)
                                        .orElseThrow(() -> new NoSuchElementException("Error in ChatSession controller test. chat session not found"));

                            }
                        }
                );
    }

}
