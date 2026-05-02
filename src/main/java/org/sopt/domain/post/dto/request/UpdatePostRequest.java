package org.sopt.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청")
public record UpdatePostRequest(
    @Schema(description = "수정할 제목", example = "수정된 제목입니다.")
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    String title,

    @Schema(description = "수정할 내용", example = "수정된 내용입니다.")
    @NotBlank(message = "내용을 입력해주세요.")
    String content
) {
}
