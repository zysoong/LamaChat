package com.example.backend.configuration;

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authorizeHttpRequests(customizer -> {
                    customizer.requestMatchers(HttpMethod.GET, "/welcome").permitAll();
                    customizer.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
                    customizer.requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll();
                    customizer.requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated();
                    customizer.requestMatchers(HttpMethod.GET, "/api/auth/me/contacts").authenticated();
                    customizer.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
                    customizer.requestMatchers(HttpMethod.GET, "/api/auth/**").authenticated();
                    customizer.requestMatchers(HttpMethod.POST, "/api/chatsessions/**").authenticated();
                    customizer.anyRequest().permitAll();
                })
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone();
    }


}
