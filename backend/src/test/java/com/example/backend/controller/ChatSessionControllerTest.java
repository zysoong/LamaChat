package com.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatSessionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenNoChatSessionExists_thenAddNewSessionToDB() throws Exception {

    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenChatSessionExists_thenDoNothing() throws Exception {

    }

    @Test
    @DirtiesContext
    @WithMockUser
    void addChatSession_whenUserDoesNotExist_thenThrowNoSuchElementException() throws Exception {

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
