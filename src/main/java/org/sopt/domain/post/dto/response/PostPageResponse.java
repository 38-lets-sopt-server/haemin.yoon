package org.sopt.domain.post.dto.response;

import java.util.List;

public record PostPageResponse(
    List<PostResponse> content,
    int page,
    int size,
    int totalElements,
    int totalPages,
    boolean isFirst,
    boolean isLast
) {}
