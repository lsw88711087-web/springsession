package com.likelion.springsession.comment.exception;

public class CommentAccessDeniedException extends RuntimeException {

    public CommentAccessDeniedException() {
        super("본인이 작성한 댓글만 수정하거나 삭제할 수 있습니다.");
    }
}