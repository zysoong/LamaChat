package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

    private static final String TEST_STANDARD_PASSWORD = "123456";

    private void registerUsers(String userName) throws Exception {

        AppUser user1 = new AppUser(
                null,
                userName,
                TEST_STANDARD_PASSWORD,
                AppUserRole.USER,
                new ArrayList<>(),
                false,
                "", ""
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());
    }

    private void addChatSessionToUsers(String userName_1, String userName_2) throws Exception{

        AppUser savedUser1 =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser2.userId())
                )
                .andExpect(status().isCreated());
    }

    private void addChatMessageToUsers(String userName_1, String userName_2, String content) throws Exception
    {
        AppUser savedUser1 =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        LocalDate localDate = LocalDate.now();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        ChatMessage msgToSend = new ChatMessage(
                null,
                savedUser1.userId(),
                savedUser2.userId(),
                date,
                content
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/message/" + savedUser1.userId() + "/" + savedUser2.userId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgToSend)))
                .andExpect(status().isCreated());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addChatSession_whenNoChatSessionExistsAndUserExists_thenAddNewSessionToDB() throws Exception {

        String userName_1 = "user1";
        String userName_2 = "user2";
        registerUsers(userName_1);
        registerUsers(userName_2);
        addChatSessionToUsers(userName_1, userName_2);

        AppUser user1AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser user2AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        String uniqueSessionId = generateSessionUniqueIdentifier(user1AfterCreation.userId(), user2AfterCreation.userId());

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
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addChatSession_whenChatSessionExistsAndUserExists_thenDoNothing() throws Exception {

        String userName_1 = "user1";
        String userName_2 = "user2";
        registerUsers(userName_1);
        registerUsers(userName_2);
        addChatSessionToUsers(userName_1, userName_2);
        addChatSessionToUsers(userName_1, userName_2);

        AppUser user1AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser user2AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        String uniqueSessionId = generateSessionUniqueIdentifier(user1AfterCreation.userId(), user2AfterCreation.userId());

        assertEquals(user1AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user2AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user1AfterCreation.chat_sessions().size(),
                1);

        assertEquals(user2AfterCreation.chat_sessions().size(),
                1);

        chatSessionRepository
                .findChatSessionByUniqueSessionIdentifier(uniqueSessionId)
                .orElseThrow(() -> new NoSuchElementException("Error in ChatSession controller test. chat session not found"));

    }


    @Test
    @DirtiesContext
    @WithMockUser(username = "user3", password = TEST_STANDARD_PASSWORD)
    void addChatSession_whenIrrelevantUserLoggedIn_thenReturnBadRequest() throws Exception {

        String userName_1 = "user1";
        String userName_2 = "user2";
        registerUsers(userName_1);
        registerUsers(userName_2);

        AppUser savedUser1 =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser2.userId())
                )
                .andExpect(status().isUnauthorized());

    }


    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addChatSession_whenUserDoesNotExist_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        "123" + "/" + "456")
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addMessageToChatSession_whenNoChatSessionExists_thenAddNewSessionAndAddMessage() throws Exception {

        String userName_1 = "user1";
        String userName_2 = "user2";
        String content = "Hi! How was it going. ";
        registerUsers(userName_1);
        registerUsers(userName_2);
        addChatMessageToUsers(userName_1, userName_2, content);

        AppUser user1AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser user2AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        String uniqueSessionId = generateSessionUniqueIdentifier(user1AfterCreation.userId(), user2AfterCreation.userId());

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

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addMessageToChatSession_whenChatSessionExists_thenAddMessage() throws Exception {

        String userName_1 = "user1";
        String userName_2 = "user2";
        String content = "Hi! How was it going. ";
        registerUsers(userName_1);
        registerUsers(userName_2);
        addChatSessionToUsers(userName_1, userName_2);
        addChatMessageToUsers(userName_1, userName_2, content);


        AppUser user1AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_1)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user1 not found")
                        );

        AppUser user2AfterCreation =
                appUserRepository
                        .findAppUserByUserName(userName_2)
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in ChatSession controller test. user2 not found")
                        );

        String uniqueSessionId = generateSessionUniqueIdentifier(user1AfterCreation.userId(), user2AfterCreation.userId());

        assertEquals(user1AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user2AfterCreation.chat_sessions().get(0).uniqueSessionIdentifier(),
                uniqueSessionId);

        assertEquals(user1AfterCreation.chat_sessions().size(),
                1);

        assertEquals(user2AfterCreation.chat_sessions().size(),
                1);

        assertEquals(user1AfterCreation.chat_sessions().get(0).chat_messages().get(0).content(),
                content);

        assertEquals(user2AfterCreation.chat_sessions().get(0).chat_messages().get(0).content(),
                content);

        chatSessionRepository
                .findChatSessionByUniqueSessionIdentifier(uniqueSessionId)
                .orElseThrow(() -> new NoSuchElementException("Error in ChatSession controller test. chat session not found"));

    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void addMessageToChatSession_whenUserDoesNotExist_thenThrowNoSuchElementException() throws Exception {

        LocalDate localDate = LocalDate.now();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String content = "Hi! How was it going?";

        ChatMessage msgToSend = new ChatMessage(
                null,
                "123",
                "456",
                date,
                content
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/message/" +
                                "123" + "/" + "456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgToSend))
                )
                .andExpect(status().isBadRequest());

    }

}
