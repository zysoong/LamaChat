package com.example.backend.security;

import com.example.backend.model.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findAppUserByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.builder()
                .username(user.userName())
                .password(user.password())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())))
                .build();
    }

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


    public AppUser findAppUserByUserName(String userName){
        return appUserRepository
                .findAppUserByUserName(userName)
                .orElseThrow(() -> new NoSuchElementException("User name " + userName + "not found!"));
    }

    public AppUser findAppUserByUserId(String userId){
        return appUserRepository
                .findAppUserByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User id " + userId + "not found!"));
    }

}
