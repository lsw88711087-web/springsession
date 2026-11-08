package com.likelion.springsession.comment.service;

import com.likelion.springsession.comment.dto.CommentCreateRequest;
import com.likelion.springsession.comment.dto.CommentResponse;
import com.likelion.springsession.comment.dto.CommentUpdateRequest;
import com.likelion.springsession.comment.entity.Comment;
import com.likelion.springsession.comment.repository.CommentRepository;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import com.likelion.springsession.user.entity.User;
import com.likelion.springsession.user.exception.UserNotFoundException;
import com.likelion.springsession.user.repository.UserRepository;
import com.likelion.springsession.comment.exception.CommentAccessDeniedException;

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
    private final UserRepository userRepository;


    // 댓글 생성
    @Transactional
    public CommentResponse addComment(
            Long userId,
            Long postId,
            CommentCreateRequest request
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "게시글을 찾을 수 없습니다."
                        )
                );

        User author = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Comment comment = new Comment(
                post,
                author,
                request.getContent()
        );

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(savedComment);
    }



    @Transactional
    public CommentResponse updateComment(
            Long userId,
            Long postId,
            Long commentId,
            CommentUpdateRequest request
    ) {

        Comment comment = findComment(postId, commentId);

        if (!comment.isAuthor(userId)){
            throw new CommentAccessDeniedException();
        }

        comment.update(request.getContent());

        return new CommentResponse(comment);
    }


    // 댓글 삭제
    @Transactional
    public void deleteComment(
            Long userId,
            Long postId,
            Long commentId
    ) {

        Comment comment = findComment(postId, commentId);

        if(!comment.isAuthor(userId)){
            throw new CommentAccessDeniedException();
        }

        commentRepository.delete(comment);
    }


    // 댓글 목록 조회
    public List<CommentResponse> getComments(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "게시글을 찾을 수 없습니다."
                        )
                );

        List<CommentResponse> responses = new ArrayList<>();

        for (Comment comment : commentRepository.findAllByPost(post)) {
            responses.add(new CommentResponse(comment));
        }

        return responses;
    }


    // 특정 게시글의 특정 댓글 찾기
    private Comment findComment(
            Long postId,
            Long commentId
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "게시글을 찾을 수 없습니다."
                        )
                );

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "댓글을 찾을 수 없습니다."
                        )
                );

        if (!comment.getPost().getId().equals(post.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 게시글의 댓글이 아닙니다."
            );
        }

        return comment;
    }
}