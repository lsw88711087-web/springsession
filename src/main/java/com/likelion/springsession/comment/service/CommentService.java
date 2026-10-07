package com.likelion.springsession.comment.service;

import com.likelion.springsession.comment.dto.CommentCreateRequest;
import com.likelion.springsession.comment.dto.CommentResponse;
import com.likelion.springsession.comment.dto.CommentUpdateRequest;
import com.likelion.springsession.comment.entity.Comment;
import com.likelion.springsession.comment.repository.CommentRepository;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentUpdateRequest request) {
        Comment comment = findComment(postId, commentId);
        comment.update(request.getContent());
        return new CommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = findComment(postId, commentId);
        comment.removeFromPost();
    }

    private Comment findComment(Long postId, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getPost().getId().equals(post.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글의 댓글이 아닙니다.");
        }

        return comment;
    }

    @Transactional
    public CommentResponse addComment(Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        Comment saved = commentRepository.save(
                new Comment(post, request.getContent())
        );

        return new CommentResponse(saved);
    }

    public List<CommentResponse> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        List<CommentResponse> responses = new ArrayList<>();

        for (Comment comment : commentRepository.findAllByPost(post)) {
            responses.add(new CommentResponse(comment));
        }

        return responses;
    }
}
