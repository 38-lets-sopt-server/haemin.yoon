package org.sopt.domain.user.exception.code;

import org.sopt.global.exception.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "해당 유저를 찾을 수 없습니다."),
  USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER4091", "이미 사용 중인 이메일입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  UserErrorCode(HttpStatus httpStatus, String code, String message) {
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
