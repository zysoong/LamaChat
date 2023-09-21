package com.example.frontendjavafx.service;

import com.example.frontendjavafx.model.ChatSession;
import com.example.frontendjavafx.security.AppUser;
import com.example.frontendjavafx.security.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ChatViewService {

    private static ChatViewService instance;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String URL_BACKEND = System.getenv("BACKEND_URI");

    public ChatViewService() {
    }

    public static synchronized ChatViewService getInstance() {
        if (instance == null) {
            instance = new ChatViewService();
        }
        return instance;
    }

    public List<AppUser> getMyContacts(){

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_BACKEND + "/api/auth/me/contacts"))
                .GET()
                .header("Cookie", "JSESSIONID=" + AuthenticationService.getInstance().getSessionId())
                .build();

        List<AppUser> result = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> mapToUserList(responseBody))
                .join();

        return result;

    }

    public AppUser getMeAsUserObject(){

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_BACKEND + "/api/auth/" + AuthenticationService.getInstance().getUsername()))
                .GET()
                .header("Cookie", "JSESSIONID=" + AuthenticationService.getInstance().getSessionId())
                .build();

        AppUser result = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> mapToAppUser(responseBody))
                .join();

        return result;
    }

    public ChatSession findOrAddChatSessionByParticipantIds(String participantOneId, String participantTwoId){

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_BACKEND + "/api/chatsessions/" + participantOneId + "/" + participantTwoId))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Cookie", "JSESSIONID=" + AuthenticationService.getInstance().getSessionId())
                .build();

        ChatSession result = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> mapToChatSession(responseBody))
                .join();

        return result;
    }

    private AppUser mapToAppUser(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, AppUser.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<AppUser> mapToUserList(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatSession mapToChatSession(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, ChatSession.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
