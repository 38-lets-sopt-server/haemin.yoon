package org.sopt.domain.post.dto.response;

import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;

// 게시글 조회 응답 (서버 → 클라이언트)
public record PostResponse(
    Long id,
    String title,
    String content,
    String author,
    String createdAt,
    BoardType boardType
) {

  // 정적 팩토리 메서드
  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getAuthor(),
        post.getCreatedAt(),
        post.getBoardType()
    );
  }
}