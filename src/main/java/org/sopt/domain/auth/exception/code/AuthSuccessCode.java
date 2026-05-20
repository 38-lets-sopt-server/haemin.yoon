package org.sopt.domain.auth.exception.code;

import org.sopt.global.exception.code.BaseSuccessCode;
import org.springframework.http.HttpStatus;

public enum AuthSuccessCode implements BaseSuccessCode {

    AUTH_LOGIN_SUCCESS(HttpStatus.OK, "AUTH2001", "로그인에 성공했습니다."),
    AUTH_REISSUE_SUCCESS(HttpStatus.OK, "AUTH2002", "토큰 재발급에 성공했습니다."),
    AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH2003", "로그아웃에 성공했습니다."),
    AUTH_GOOGLE_LOGIN_SUCCESS(HttpStatus.OK, "AUTH2004", "Google 로그인에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthSuccessCode(HttpStatus httpStatus, String code, String message) {
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
