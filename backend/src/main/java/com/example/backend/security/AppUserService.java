package com.example.backend.security;

import com.example.backend.model.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService  {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(AppUserRequest appUserRequest){

        AppUser userToSave = new AppUser(
                null,
                appUserRequest.userName(),
                passwordEncoder.encode(appUserRequest.password()),
                AppUserRole.USER,
                new ArrayList<ChatSession>(),
                appUserRequest.isVirtualAgent(),
                appUserRequest.api(),
                appUserRequest.apiKey()
        );

        appUserRepository.save(userToSave);
    }

}
