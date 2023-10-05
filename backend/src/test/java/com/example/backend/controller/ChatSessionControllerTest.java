package com.example.backend.controller;

import com.example.backend.model.ChatSession;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.security.AppUser;
import com.example.backend.security.AppUserRepository;
import com.example.backend.security.AppUserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static com.example.backend.utilities.SessionIdentifierUtilities.generateSessionUniqueIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatSessionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenNoChatSessionExistsAndUserExists_thenAddNewSessionToDB() throws Exception {

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                                savedUser1.userId() + "/" + savedUser2.userId())
                        )
                .andExpect(status().isCreated());

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

        String uniqueSessionId = generateSessionUniqueIdentifier(savedUser1.userId(), savedUser2.userId());

        assertEquals(user1AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user2AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        chatSessionRepository
                .findChatSessionByUniqueSessionIdentifier(uniqueSessionId)
                .orElseThrow(() -> new NoSuchElementException("Error in ChatSession controller test. chat session not found"));
    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenChatSessionExistsAndUserExists_thenDoNothing() throws Exception {

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser2.userId())
                )
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser2.userId())
                )
                .andExpect(status().isCreated());

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

        String uniqueSessionId = generateSessionUniqueIdentifier(savedUser1.userId(), savedUser2.userId());

        assertEquals(user1AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user2AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        chatSessionRepository
                .findChatSessionByUniqueSessionIdentifier(uniqueSessionId)
                .orElseThrow(() -> new NoSuchElementException("Error in ChatSession controller test. chat session not found"));

    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenUserDoesNotExist_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        "123" + "/" + "456")
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addMessageToChatSession_whenNoChatSessionExists_thenAddNewSessionAndAddMessage() throws Exception {
        
    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addMessageToChatSession_whenChatSessionExists_thenAddMessage() throws Exception {

    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addMessageToChatSession_whenUserDoesNotExist_thenThrowNoSuchElementException() throws Exception {

    }

}
