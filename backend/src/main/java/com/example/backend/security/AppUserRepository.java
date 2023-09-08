package com.example.backend.security;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppUserRepository extends MongoRepository<AppUser, String>
{
    Optional<AppUser> findAppUserByUserId(String userId);
    Optional<AppUser> findAppUserByUserName(String userName);
}
