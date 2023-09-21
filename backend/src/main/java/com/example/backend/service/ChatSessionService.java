package com.example.backend.service;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatSession;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.security.AppUser;
import com.example.backend.security.AppUserRepository;
import com.example.backend.utilities.SessionIdentifierUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class ChatSessionService
{
    private final ChatSessionRepository chatSessionRepository;
    private final AppUserRepository appUserRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatSession getChatSession(String participantOneId, String participantTwoId)
    {
        AppUser participantOne = appUserRepository
                .findAppUserByUserId(participantOneId)
                .orElseThrow(() -> new NoSuchElementException("AppUser with Id " + participantOneId + " was not found. "));

        AppUser participantTwo = appUserRepository
                .findAppUserByUserId(participantTwoId)
                .orElseThrow(() -> new NoSuchElementException("AppUser with Id " + participantTwoId + " was not found. "));

        String sessionIdentifier = SessionIdentifierUtilities.generateSessionUniqueIdentifier(participantOneId, participantTwoId);

        return chatSessionRepository
                .findChatSessionByUniqueSessionIdentifier(sessionIdentifier)
                .orElseThrow(() -> new NoSuchElementException("Session with Id " + sessionIdentifier + " was not found. "));
    }

    public ChatSession addChatSessionOverride(String participantOneId, String participantTwoId)
    {

        AppUser participantOne = appUserRepository
                .findAppUserByUserId(participantOneId)
                .orElseThrow(() -> new NoSuchElementException("AppUser with Id " + participantOneId + " was not found. "));

        AppUser participantTwo = appUserRepository
                .findAppUserByUserId(participantTwoId)
                .orElseThrow(() -> new NoSuchElementException("AppUser with Id " + participantTwoId + " was not found. "));

        String sessionIdentifier = SessionIdentifierUtilities.generateSessionUniqueIdentifier(participantOneId, participantTwoId);

        AtomicBoolean isChatSessionFound = new AtomicBoolean(true);

        ChatSession newChatSession =
                chatSessionRepository
                        .findChatSessionByUniqueSessionIdentifier(sessionIdentifier)
                        .orElseGet( () ->
                                {
                                    isChatSessionFound.set(false);
                                    return new ChatSession(
                                            null,
                                            sessionIdentifier,
                                            new ArrayList<ChatMessage>()
                                    );
                                }
                        );

        if (!isChatSessionFound.get()){

            ChatSession addChatSession = chatSessionRepository.save(newChatSession);

            participantOne.chat_sessions().add(addChatSession);
            appUserRepository.save(participantOne);

            participantTwo.chat_sessions().add(addChatSession);
            appUserRepository.save(participantTwo);

            return addChatSession;

        } else {
            return newChatSession;
        }
    }

    public ChatMessage addChatMessageToChatSession(String participantOneId, String participantTwoId, ChatMessage msg)
    {

        ChatSession session = addChatSessionOverride(participantOneId, participantTwoId);

        ChatMessage messageToBeSent = new ChatMessage(
                null,
                msg.senderId(),
                msg.recipientId(),
                msg.timestamp(),
                msg.content()
        );

        ChatMessage messageSent = chatMessageRepository.save(messageToBeSent);
        session.chat_messages().add(messageSent);
        chatSessionRepository.save(session);

        return messageSent;

    }


}
