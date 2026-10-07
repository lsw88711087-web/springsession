package com.likelion.springsession.comment.entity;

import com.likelion.springsession.post.entity.Post;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public Comment(Post post, String content) {
        this.post = post;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        post.getComments().add(this);
    }

    public void update(String content) {
        this.content = content;
    }

    public void removeFromPost() {
        this.post.getComments().remove(this);
        this.post = null;
    }
}
