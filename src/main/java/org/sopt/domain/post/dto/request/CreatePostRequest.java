package org.sopt.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sopt.domain.post.entity.BoardType;

@Schema(description = "게시글 생성 요청")
public record CreatePostRequest(
    @Schema(description = "게시글 제목", example = "Let's SOPT!")
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    String title,

    @Schema(description = "게시글 내용", example = "SOPT 화이팅!")
    @NotBlank(message = "내용을 입력해주세요.")
    String content,

    @Schema(description = "작성자 ID", example = "1")
    @NotNull(message = "작성자 ID는 필수입니다.")
    Long userId,

    @Schema(description = "게시판 종류", example = "FREE")
    @NotNull(message = "게시판 종류는 필수입니다.")
    BoardType boardType
) {}
