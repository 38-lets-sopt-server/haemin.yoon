package org.sopt.domain.auth.exception.code;

import org.sopt.global.exception.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BaseErrorCode {

    // Spring Security 필터 단에서 발생 — GlobalExceptionHandler가 아닌 EntryPoint/Handler가 처리
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH4010", "인증이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH4030", "접근 권한이 없습니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4011", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4012", "회원이 존재하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "유효하지 않은 토큰입니다."),
    // 토큰이 만료/위변조된 경우와 구별 — DB에서 이미 삭제된(로그아웃/재발급된) 토큰
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4014", "Refresh Token이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
