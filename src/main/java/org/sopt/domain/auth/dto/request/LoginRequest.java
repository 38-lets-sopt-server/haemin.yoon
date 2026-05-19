package org.sopt.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        // 이메일·비밀번호를 @RequestParam(쿼리스트링)으로 받으면
        // 서버 액세스 로그, 브라우저 히스토리, 프록시 로그에 평문이 그대로 남음
        // @RequestBody로 받아야 HTTPS 암호화 페이로드에 포함되어 노출되지 않음
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {}
