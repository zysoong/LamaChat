package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatSession;
import com.example.backend.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatsessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping("/{participantOneId}/{participantTwoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatSession addChatSession(@PathVariable("participantOneId") String participantOneId, @PathVariable("participantTwoId") String participantTwoId)
    {
        return chatSessionService.addChatSessionOverride(participantOneId, participantTwoId);
    }

    @PostMapping("message/{participantOneId}/{participantTwoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addChatSession(@PathVariable("participantOneId") String participantOneId,
                               @PathVariable("participantTwoId") String participantTwoId,
                               @RequestBody ChatMessage msg)
    {
        chatSessionService.addChatMessageToChatSession(participantOneId, participantTwoId, msg);
    }

}
