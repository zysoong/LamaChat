package com.example.backend.security;

import com.example.backend.exception.IllegalAuthenticationException;
import com.example.backend.model.ChatSession;
import com.example.backend.utilities.SessionIdentifierUtilities;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("/me")
    public String getMe(Principal principal){
        if (principal != null) {
            return principal.getName();
        }
        throw new IllegalAuthenticationException("Authentication is invalid or removed accidentally. ");
    }

    @GetMapping("/me/contacts")
    public List<AppUserDTO> getMyContacts(Principal principal){
        if (principal != null) {

            AppUser me = appUserService.findAppUserByUserName(principal.getName());
            ArrayList<AppUserDTO> res = new ArrayList<>();
            for (ChatSession session : me.chat_sessions()){

                AppUser originalUserInfoToBeAddedLater =
                        appUserService.findAppUserByUserId(
                                SessionIdentifierUtilities.getReceiverFromUniqueIdentifier(session.uniqueSessionIdentifier(), me.userId())
                        );

                AppUserDTO userDtoToAdd =
                        new AppUserDTO(
                                originalUserInfoToBeAddedLater.userId(),
                                originalUserInfoToBeAddedLater.userName(),
                                false
                        );

                res.add(userDtoToAdd);
            }
            return res;
        }
        return new ArrayList<>();
    }

    @GetMapping("/{userName}")
    public AppUserDTO getByUserName(@PathVariable String userName, Principal principal){

        AppUser originalAppUser = appUserService.findAppUserByUserName(userName);

        return new AppUserDTO(
                originalAppUser.userId(),
                originalAppUser.userName(),
                false
        );

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(Principal principal){
        String loggedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3600, TimeUnit.MINUTES))
                .body(loggedUser);
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody AppUserRequest appUserRequest){
        appUserService.createUser(appUserRequest);
    }

    @PostMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        SecurityContextHolder.clearContext();
        return "anonymousUser";
    }
}
