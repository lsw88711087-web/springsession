package com.likelion.springsession.post.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;    // ← 추가
    private final LocalDateTime createdAt;
}
