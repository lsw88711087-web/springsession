package com.likelion.springsession.auth.controller;

import com.likelion.springsession.auth.dto.SignUpRequest;
import com.likelion.springsession.auth.dto.SignUpResponse;
import com.likelion.springsession.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.likelion.springsession.auth.dto.TokenResponse;
import com.likelion.springsession.auth.dto.ReissueRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.likelion.springsession.auth.dto.LoginRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
    }
}