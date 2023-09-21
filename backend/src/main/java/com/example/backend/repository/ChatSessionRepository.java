package com.example.backend.repository;

import com.example.backend.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String>
{
    Optional<ChatSession> findChatSessionByUniqueSessionIdentifier(String uniqueSessionIdentifier);
}
