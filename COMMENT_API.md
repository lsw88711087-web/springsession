# 4주차 댓글 수정·삭제 API

기준 자료: https://terrific-bench-846.notion.site/4-5e931636d6cb8347afc201f5c1a2caf5

기존 프로젝트의 `Controller → Service → Repository` 구조를 유지했습니다.

## 댓글 수정

`PUT /api/posts/{postId}/comments/{commentId}`

Postman에서 Body → raw → JSON으로 요청합니다.

```json
{
  "content": "수정할 댓글 내용"
}
```

- 성공: `200 OK`, 댓글의 `id`, `content`, `createdAt` 반환.
- 내용 누락·null·빈 문자열·공백·500자 초과: `400 Bad Request`.
- 게시글이나 댓글이 없거나 다른 게시글의 댓글인 경우: `404 Not Found`.

`CommentUpdateRequest`에서 입력을 검증합니다. `CommentService.updateComment()`는 게시글과 댓글을 조회하고 댓글이 해당 게시글에 속하는지 확인합니다. 그다음 `Comment.update()`로 내용만 변경합니다. 작성 시각은 변경하지 않습니다.

서비스의 `@Transactional` 안에서 조회한 엔티티를 수정하므로 JPA 변경 감지가 DB에 반영합니다. `save()`를 다시 호출하지 않습니다.

## 댓글 삭제

`DELETE /api/posts/{postId}/comments/{commentId}`

요청 본문은 필요하지 않습니다.

- 성공: `204 No Content`, 빈 응답 본문.
- 게시글이나 댓글이 없거나 다른 게시글의 댓글인 경우: `404 Not Found`.

`CommentService.deleteComment()`에서도 게시글과 댓글의 소속을 먼저 확인합니다. `Comment.removeFromPost()`는 `post.getComments()`에서 자신을 제거하고 자신의 `post` 참조를 null로 정리합니다.

`Post.comments`의 `orphanRemoval = true` 때문에 트랜잭션이 반영될 때 목록에서 제거한 댓글이 DB에서도 삭제됩니다. `commentRepository.delete()`를 호출하지 않습니다. 기존 `CascadeType.REMOVE`도 유지해 게시글을 삭제하면 연결된 댓글 전체가 삭제됩니다.

## 변경 파일

- `comment/controller/CommentController.java`: PUT·DELETE 경로 추가.
- `comment/dto/CommentUpdateRequest.java`: 수정 요청 DTO 추가.
- `comment/dto/CommentResponse.java`: JSON 응답을 위한 `@Getter` 추가.
- `comment/service/CommentService.java`: 수정·삭제, 게시글과 댓글 조회·소속 확인 추가.
- `comment/entity/Comment.java`: 내용 수정·양쪽 참조 제거 메서드 추가, 게시글 LAZY 조회 적용.
- `post/entity/Post.java`: `orphanRemoval = true` 추가.

## 검증

`src/test/java/com/likelion/springsession/comment/CommentApiTests.java`에 실제 HTTP 요청 계층과 JPA를 함께 검증하는 테스트를 추가했습니다. 서비스 트랜잭션이 완료된 뒤 데이터를 다시 조회해 변경 감지와 고아 객체 삭제가 DB에 반영되는지 검사합니다.

검증 항목은 수정 후 목록 재조회, 작성 시각 유지, 500자 허용·501자 거절, 빈 내용 거절, 삭제 후 게시글·다른 댓글 유지, 양방향 참조 정리, 잘못된 게시글의 댓글 보호, 존재하지 않는 ID 거절, 게시글 삭제 전파입니다.

테스트용 H2 의존성과 `src/test/resources/application.yaml`을 추가했습니다. 테스트에서는 MySQL과 `DB_PASSWORD`가 필요하지 않습니다. 앱 실행 시에는 기존 MySQL 설정을 사용합니다.

Windows PowerShell에서 프로젝트 폴더를 열고 실행합니다.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat test --no-daemon
```

이 작업에서는 Gradle 캐시 접근 권한 요청이 거절되어 테스트를 실행하지 못했습니다.

## Postman 확인 순서

1. `POST /api/posts`로 A·B 게시글을 만들고 각 ID를 기록합니다.
2. `POST /api/posts/{A의 ID}/comments`로 댓글 두 개를 만들고 응답의 댓글 ID와 작성 시각을 기록합니다.
3. A 게시글의 URL로 댓글 하나를 수정합니다. 200 응답과 작성 시각 유지, GET 댓글 목록의 수정 내용을 확인합니다.
4. B 게시글의 수정·삭제 URL에 A의 댓글 ID를 넣습니다. 404 응답 후 A의 댓글이 그대로 남아 있는지 조회합니다.
5. 수정 요청에 빈 문자열·공백·501자 내용을 보내 400인지 확인합니다.
6. A 게시글의 URL로 댓글 하나를 삭제합니다. 204와 빈 본문, GET 목록에서 해당 댓글만 제거된 것을 확인합니다.
7. 존재하지 않는 게시글·댓글 ID로 수정·삭제를 요청해 404인지 확인합니다.
8. A 게시글을 삭제하고 DB에서 연결된 댓글도 함께 삭제됐는지 확인합니다.
