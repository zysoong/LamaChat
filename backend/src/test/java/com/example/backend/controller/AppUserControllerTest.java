package com.example.backend.controller;

import com.example.backend.security.AppUser;
import com.example.backend.security.AppUserIdAndNameDTO;
import com.example.backend.security.AppUserRepository;
import com.example.backend.security.AppUserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Base64;
import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_STANDARD_PASSWORD = "123456";

    @ParameterizedTest
    @DirtiesContext
    @ValueSource(strings = {"user1"})
    void registerUser(String userName) throws Exception {

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

    @Test
    @DirtiesContext
    void loginUser_whenUserExists_thenReturnOK() throws Exception {
        registerUser("user1");
        mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/auth/login")
                    .header("Authorization", "Basic " +
                            Base64.getEncoder().encodeToString(("user1" + ":" + TEST_STANDARD_PASSWORD).getBytes()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    void loginUser_whenUserNotExists_thenReturnUnauthorizedError() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/login")
                                .header("Authorization", "Basic " +
                                        Base64.getEncoder().encodeToString(("user1" + ":" + TEST_STANDARD_PASSWORD).getBytes()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void getMe_whenUserLoggedIn_thenReturnUserName() throws Exception {
        mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/auth/me")
                )
                .andExpect(content().string("user1"))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    void getMe_whenUserNotLoggedIn_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/auth/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void getMyContacts_whenCalled_thenListAllContacts() throws Exception {

        registerUser("user1");
        registerUser("user2");
        registerUser("user3");
        registerUser("user4");

        AppUser savedUser1 =
                appUserRepository
                        .findAppUserByUserName("user1")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in AppUser controller test. user1 not found")
                        );

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName("user2")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in AppUser controller test. user2 not found")
                        );

        AppUser savedUser3 =
                appUserRepository
                        .findAppUserByUserName("user3")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in AppUser controller test. user3 not found")
                        );

        AppUser savedUser4 =
                appUserRepository
                        .findAppUserByUserName("user4")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in AppUser controller test. user4 not found")
                        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser2.userId())
                )
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser3.userId())
                )
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/chatsessions/" +
                        savedUser1.userId() + "/" + savedUser4.userId())
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/auth/me/contacts")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("user2"));

    }


    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void getUser_whenUserExists_thenReturnUserDTO() throws Exception {

        registerUser("user1");
        registerUser("user2");

        AppUser savedUser2 =
                appUserRepository
                        .findAppUserByUserName("user2")
                        .orElseThrow(() ->
                                new NoSuchElementException("Error in AppUser controller test. user2 not found")
                        );

        AppUserIdAndNameDTO expectedResponse = new AppUserIdAndNameDTO(savedUser2.userId(), "user2");

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/auth/user2")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user1", password = TEST_STANDARD_PASSWORD)
    void getUser_whenUserNotExists_thenReturnBadRequest() throws Exception {


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/auth/user2")
                )
                .andExpect(status().isBadRequest());

    }

}
