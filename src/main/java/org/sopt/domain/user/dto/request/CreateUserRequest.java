package org.sopt.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 가입 요청")
public record CreateUserRequest(
    @Schema(description = "닉네임", example = "솝트햄")
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 20, message = "닉네임은 20자 이내로 입력해주세요.")
    String nickname,

    @Schema(description = "이메일", example = "sopt@example.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "비밀번호", example = "sopt1234!")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    String password
) {}
