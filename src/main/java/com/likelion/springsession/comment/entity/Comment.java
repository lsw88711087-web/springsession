package com.likelion.springsession.comment.entity;

import com.likelion.springsession.post.entity.Post;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.likelion.springsession.user.entity.User;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    @ManyToOne(fetch = FetchType.LAZY)              // ← 추가
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    public Comment(Post post, User author, String content) {   // ← 생성자 수정
        this.post = post;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        post.getComments().add(this);      // 4주차 연관관계 편의 메서드 -그대로 유지
    }

    public boolean isAuthor(Long userId) {          // ← 추가 (과제에서 씁니다)
        return this.author.getId().equals(userId);
    }

    public void update(String content) {
        this.content = content;
    }

    public void removeFromPost() {
        this.post.getComments().remove(this);
        this.post = null;
    }
}
