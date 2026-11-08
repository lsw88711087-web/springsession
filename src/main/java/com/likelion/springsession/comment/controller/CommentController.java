package com.likelion.springsession.comment.controller;

import com.likelion.springsession.comment.dto.CommentCreateRequest;
import com.likelion.springsession.comment.dto.CommentResponse;
import com.likelion.springsession.comment.dto.CommentUpdateRequest;
import com.likelion.springsession.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PutMapping("/{commentId}")
    public CommentResponse update(
            @AuthenticationPrincipal Long userId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        return commentService.updateComment(
                userId, postId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        commentService.deleteComment(userId, postId, commentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @AuthenticationPrincipal Long userId,          // ← 추가
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentCreateRequest request) {

        return commentService.addComment(userId, postId, request);
    }

    @GetMapping
    public List<CommentResponse> list(
            @PathVariable("postId") Long postId) {

        return commentService.getComments(postId);
    }
}
