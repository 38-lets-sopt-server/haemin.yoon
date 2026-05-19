package org.sopt.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.domain.user.entity.User;

@Schema(description = "유저 정보 응답")
public record UserResponse(
        @Schema(description = "유저 ID", example = "1")
        Long id,
        @Schema(description = "닉네임", example = "솝트햄")
        String nickname,
        @Schema(description = "이메일", example = "sopt@example.com")
        String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getEmail());
    }
}
