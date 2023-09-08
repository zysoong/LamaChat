package com.example.backend.repository;

import com.example.backend.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String>
{

}
