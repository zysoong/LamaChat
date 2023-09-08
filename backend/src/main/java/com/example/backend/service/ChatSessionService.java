package com.example.backend.service;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatSession;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.security.AppUser;
import com.example.backend.security.AppUserRepository;
import com.example.backend.utilities.SessionIdentifierUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class ChatSessionService
{
    private final ChatSessionRepository chatSessionRepository;
    private final AppUserRepository appUserRepository;

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

    /**
     * Create a new chat session with empty message list.
     * If one of the participants not exists, then throw NoSuchElementException.
     * If chat session already exists, do nothing, otherwise create a new chat session in DB and assign it to the
     *session lists of both participants
     * @param participantOneId
     * @param participantTwoId
     * @return
     */
    public ChatSession addChatSession(String participantOneId, String participantTwoId)
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

        }
        return newChatSession;
    }

    /**
     * Push a chat message to chat session.
     * If the chat session does not exist, then create a new one with the "addChatSession" function
     * @param participantOneId
     * @param participantTwoId
     * @param msg
     */
    public void addChatMessageToChatSession(String participantOneId, String participantTwoId, ChatMessage msg)
    {
        ChatSession session = addChatSession(participantOneId, participantTwoId);
        session.chat_messages().add(msg);
    }


}
