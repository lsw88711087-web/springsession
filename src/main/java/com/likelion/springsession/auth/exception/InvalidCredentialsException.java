package com.likelion.springsession.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
    }
}