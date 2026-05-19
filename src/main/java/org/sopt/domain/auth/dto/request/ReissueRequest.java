package org.sopt.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueRequest(
    // 토큰을 URL 파라미터로 보내면 서버 로그·브라우저 히스토리에 노출될 수 있어 body 전송이 원칙
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "Refresh Token은 필수입니다.")
    String refreshToken
) {}
