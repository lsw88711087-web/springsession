package com.likelion.springsession.auth.dto;
// auth/dto/TokenResponse.java

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
}