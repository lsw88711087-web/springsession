package com.likelion.springsession.post.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostSummaryResponse {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;

}