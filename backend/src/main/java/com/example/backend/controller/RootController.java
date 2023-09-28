package com.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/welcome")
@RequiredArgsConstructor
public class RootController {

    @GetMapping
    public String getWelccome() {
        return "Welcome";
    }

}
