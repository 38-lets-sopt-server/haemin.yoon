package org.sopt.domain.post.dto.request;

// 게시글 작성 요청 (클라이언트 → 서버)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sopt.domain.post.entity.BoardType;

public record CreatePostRequest(
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    String title,

    @NotBlank(message = "내용을 입력해주세요.")
    String content,

    @NotBlank(message = "작성자는 필수입니다.")
    String author,

    @NotNull(message = "게시판 종류는 필수입니다.")
    BoardType boardType
) {
}