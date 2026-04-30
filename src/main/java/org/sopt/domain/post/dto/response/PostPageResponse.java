package org.sopt.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "게시글 페이징 응답")
public record PostPageResponse(
    @Schema(description = "게시글 목록")
    List<PostResponse> content,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지당 데이터 개수", example = "10")
    int size,

    @Schema(description = "전체 데이터 개수", example = "125")
    int totalElements,

    @Schema(description = "전체 페이지 수", example = "13")
    int totalPages,

    @Schema(description = "첫 페이지 여부", example = "true")
    boolean isFirst,

    @Schema(description = "마지막 페이지 여부", example = "false")
    boolean isLast
) {}
