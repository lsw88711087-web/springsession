package com.likelion.springsession.post.controller;

import com.likelion.springsession.post.dto.PostDetailResponse;
import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.service.PostService;
import com.likelion.springsession.post.dto.PostUpdateRequest;

import java.util.List;

import com.likelion.springsession.post.dto.PostCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping()
    public List<PostSummaryResponse> getPosts() {
        return postService.getPostSummaries();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse createPost(
            @Valid @RequestBody PostCreateRequest request
    ){
        return postService.createPost(request);
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPost(
            @PathVariable("postId") Long postId
    ){
        return postService.getPost(postId);
    }

    @PutMapping("/{postId}")
    public PostDetailResponse updatePost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ){
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("postId") Long postId){
        postService.deletePost(postId);
    }
}