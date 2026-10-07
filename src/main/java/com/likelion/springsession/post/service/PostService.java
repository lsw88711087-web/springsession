package com.likelion.springsession.post.service;

import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.dto.PostUpdateRequest;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostDetailResponse createPost(PostCreateRequest request){

        Post post = new Post(
                request.getTitle(),
                request.getContent()
        );

        Post savedPost = postRepository.save(post);

        return toDetailResponse(savedPost);
    }

    private PostDetailResponse toDetailResponse(Post post){
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }

    public PostDetailResponse getPost(Long postId){
        Post post = findPostById(postId);
        return toDetailResponse(post);
    }

    private Post findPostById(Long postId){
        return postRepository.findById(postId)
                .orElseThrow();
    }

    @Transactional
    public PostDetailResponse updatePost(
            Long postId,
            PostUpdateRequest request
    ){
        Post post = findPostById(postId);

        post.update(
            request.getTitle(),
            request.getContent()
        );

        return toDetailResponse(post);
    }

    @Transactional
    public void deletePost(Long postId){
        Post post = findPostById(postId);

        postRepository.delete(post);
    }


    public List<PostSummaryResponse> getPostSummaries() {
        List<Post> posts = postRepository.findAll();
        List<PostSummaryResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            PostSummaryResponse response = new PostSummaryResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getCreatedAt()
            );

            responses.add(response);
        }

        return responses;
    }
}