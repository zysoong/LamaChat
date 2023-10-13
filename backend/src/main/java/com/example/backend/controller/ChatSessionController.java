package com.example.backend.controller;

import com.example.backend.exception.IllegalAuthenticationException;
import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatMessageSendDTO;
import com.example.backend.model.ChatSession;
import com.example.backend.security.AppUserService;
import com.example.backend.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/chatsessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final AppUserService appUserService;

    @PostMapping("/{participantOneId}/{participantTwoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatSession addChatSession(
            @PathVariable("participantOneId") String participantOneId,
            @PathVariable("participantTwoId") String participantTwoId,
            Principal principal)
    {
        String loggedInUserName = principal.getName();
        String participantOneUserName = appUserService.findAppUserByUserId(participantOneId).userName();
        String participantTwoUserName = appUserService.findAppUserByUserId(participantTwoId).userName();

        if (loggedInUserName.equals(participantOneUserName) || loggedInUserName.equals(participantTwoUserName))
        {
            return chatSessionService.addChatSessionOverride(participantOneId, participantTwoId);
        }
        else
        {
            throw new IllegalAuthenticationException();
        }

    }

    @PostMapping("message/{participantOneId}/{participantTwoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMessageToChatSession(@PathVariable("participantOneId") String participantOneId,
                                        @PathVariable("participantTwoId") String participantTwoId,
                                        @RequestBody ChatMessageSendDTO msg,
                                        Principal principal)
    {

        String loggedInUserName = principal.getName();
        String participantOneUserName = appUserService.findAppUserByUserId(participantOneId).userName();
        String participantTwoUserName = appUserService.findAppUserByUserId(participantTwoId).userName();

        if (loggedInUserName.equals(participantOneUserName) || loggedInUserName.equals(participantTwoUserName))
        {
            ChatMessage convertedMsg = new ChatMessage(
                    msg.messageId(),
                    msg.senderId(),
                    msg.recipientId(),
                    msg.timestamp(),
                    msg.content()
            );

            chatSessionService.addChatMessageToChatSession(participantOneId, participantTwoId, convertedMsg);
        }
        else
        {
            throw new IllegalAuthenticationException();
        }
    }

}
