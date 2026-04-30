package org.sopt.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;

@Schema(description = "게시글 단건 응답")
public record PostResponse(
    @Schema(description = "게시글 ID", example = "1")
    Long id,
    @Schema(description = "제목", example = "제목입니다.")
    String title,
    @Schema(description = "내용", example = "내용입니다.")
    String content,
    @Schema(description = "작성자 닉네임", example = "솝트햄")
    String author,
    @Schema(description = "생성일시", example = "2024-04-26 11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime createdAt,
    @Schema(description = "수정일시", example = "2024-04-26 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime updatedAt,
    @Schema(description = "게시판 종류", example = "FREE")
    BoardType boardType
) {
  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getUser().getNickname(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getBoardType()
    );
  }
}
