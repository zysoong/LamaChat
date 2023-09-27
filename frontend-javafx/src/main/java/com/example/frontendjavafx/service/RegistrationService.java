package com.example.frontendjavafx.service;

import com.example.frontendjavafx.security.AppUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegistrationService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String URL_BACKEND = System.getenv("BACKEND_URI");

    private static RegistrationService INSTANCE;

    private RegistrationService() {
    }

    public static synchronized RegistrationService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RegistrationService();
        }
        return INSTANCE;
    }

    public boolean createNewAppUser(AppUser appUserToCreate){

        int statusCode = -1;

        try{

            String requestBody = objectMapper.writeValueAsString(appUserToCreate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BACKEND + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            statusCode = response.join().statusCode();

        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }

        if (statusCode == 201 || statusCode == 200){
            return true;
        } else {
            return false;
        }

    }
}
