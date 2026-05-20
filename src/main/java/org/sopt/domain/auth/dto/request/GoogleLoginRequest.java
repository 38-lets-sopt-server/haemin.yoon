package org.sopt.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google 소셜 로그인 요청")
public record GoogleLoginRequest(
        // 클라이언트가 Google 인가 화면에서 받은 authorization code
        // 1회성이며 수 분 내 만료됨 — 서버가 이 코드로 Google에서 access token을 교환함
        @Schema(description = "Google 인가 코드", example = "4/0AX4XfWh...")
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code,

        // 클라이언트가 Google에 등록한 redirect URI와 반드시 일치해야 함
        // Google이 code를 발급할 때 사용한 redirect URI를 token 교환 시 재검증함
        @Schema(description = "Google OAuth redirect URI", example = "http://localhost:3000/oauth/callback")
        @NotBlank(message = "redirect URI는 필수입니다.")
        String redirectUri
) {}
