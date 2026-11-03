package com.likelion.springsession.comment;

import com.likelion.springsession.comment.entity.Comment;
import com.likelion.springsession.comment.repository.CommentRepository;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import com.likelion.springsession.security.JwtProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.likelion.springsession.user.entity.User;
import com.likelion.springsession.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
class CommentApiTests {

    @Autowired MockMvc mockMvc;
    @Autowired PostRepository postRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired TransactionTemplate transactions;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private String accessToken;

    private Long postId;
    private Long otherPostId;
    private Long commentId;
    private Long siblingId;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        transactions.executeWithoutResult(status -> {
            commentRepository.deleteAll();
            postRepository.deleteAll();
            User author = userRepository.findByEmail("test@likelion.com")
                    .orElseGet(() -> userRepository.save(new User(
                            "test@likelion.com",
                            passwordEncoder.encode("password1"),
                            "테스트 회원")));
            accessToken = jwtProvider.createAccessToken(author.getId());
            Post post = postRepository.save(new Post(author, "A", "본문 A"));
            Post other = postRepository.save(new Post(author, "B", "본문 B"));
            Comment comment = commentRepository.save(new Comment(post, author, "원래 댓글"));
            Comment sibling = commentRepository.save(new Comment(post, author, "다른 댓글"));
            postId = post.getId();
            otherPostId = other.getId();
            commentId = comment.getId();
            siblingId = sibling.getId();
        });
        createdAt = commentRepository.findById(commentId).orElseThrow().getCreatedAt();
    }

    @Test
    void updatePersistsContentAndKeepsCreatedAt() throws Exception {
        mockMvc.perform(put(url(postId, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정한 댓글\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value("수정한 댓글"))
                .andExpect(jsonPath("$.createdAt").exists());

        Comment reloaded = commentRepository.findById(commentId).orElseThrow();
        assertThat(reloaded.getContent()).isEqualTo("수정한 댓글");
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + commentId + ")].content").value("수정한 댓글"));
    }

    @Test
    void deleteUsesOrphanRemovalAndKeepsPostAndSibling() throws Exception {
        mockMvc.perform(delete(url(postId, commentId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(commentRepository.existsById(commentId)).isFalse();
        assertThat(commentRepository.existsById(siblingId)).isTrue();
        assertThat(postRepository.existsById(postId)).isTrue();
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(siblingId));
    }

    @Test
    void removingCommentClearsBothReferences() {
        transactions.executeWithoutResult(status -> {
            Comment comment = commentRepository.findById(commentId).orElseThrow();
            Post post = comment.getPost();
            comment.removeFromPost();
            assertThat(comment.getPost()).isNull();
            assertThat(post.getComments()).doesNotContain(comment);
        });
        assertThat(commentRepository.existsById(commentId)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"content\":\"\"}", "{\"content\":\"   \"}", "{\"content\":null}", "{}"})
    void rejectsEmptyContent(String body) throws Exception {
        mockMvc.perform(put(url(postId, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        assertOriginalComment();
    }

    @Test
    void rejectsMoreThan500Characters() throws Exception {
        mockMvc.perform(put(url(postId, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + "a".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
        assertOriginalComment();
    }

    @Test
    void accepts500Characters() throws Exception {
        mockMvc.perform(put(url(postId, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + "a".repeat(500) + "\"}"))
                .andExpect(status().isOk());
        assertThat(commentRepository.findById(commentId).orElseThrow().getContent()).hasSize(500);
    }

    @Test
    void cannotUpdateAnotherPostsComment() throws Exception {
        mockMvc.perform(put(url(otherPostId, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"바뀌면 안 됨\"}"))
                .andExpect(status().isNotFound());
        assertOriginalComment();
    }

    @Test
    void cannotDeleteAnotherPostsComment() throws Exception {
        mockMvc.perform(delete(url(otherPostId, commentId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
        assertOriginalComment();
    }

    @Test
    void rejectsMissingPostForBothOperations() throws Exception {
        mockMvc.perform(put(url(Long.MAX_VALUE, commentId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(url(Long.MAX_VALUE, commentId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
        assertOriginalComment();
    }

    @Test
    void rejectsMissingCommentForBothOperations() throws Exception {
        mockMvc.perform(put(url(postId, Long.MAX_VALUE))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(url(postId, Long.MAX_VALUE))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
        assertOriginalComment();
    }

    @Test
    void deletingPostStillCascadesToComments() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
        assertThat(commentRepository.existsById(commentId)).isFalse();
        assertThat(commentRepository.existsById(siblingId)).isFalse();
        assertThat(postRepository.existsById(otherPostId)).isTrue();
    }

    private void assertOriginalComment() {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        assertThat(comment.getContent()).isEqualTo("원래 댓글");
        assertThat(comment.getCreatedAt()).isEqualTo(createdAt);
    }

    private String url(Long postId, Long commentId) {
        return "/api/posts/" + postId + "/comments/" + commentId;
    }
}
