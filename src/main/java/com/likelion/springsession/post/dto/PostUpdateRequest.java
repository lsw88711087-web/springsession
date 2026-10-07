package com.likelion.springsession.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max =100, message ="제목은 100자이하로 작성해주세요")
    private String title;

    @NotBlank(message = "본문을 필수입니다.")
    @Size(max = 2000, message = "본문을 2000자이하로 작성해주세요.")
    private String content;
}
