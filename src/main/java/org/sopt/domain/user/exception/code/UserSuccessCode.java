package org.sopt.domain.user.exception.code;

import org.sopt.global.exception.code.BaseSuccessCode;
import org.springframework.http.HttpStatus;

public enum UserSuccessCode implements BaseSuccessCode {

  USER_JOIN_SUCCESS(HttpStatus.CREATED, "USER2011", "회원 가입에 성공했습니다."),
  USER_GET_ME_SUCCESS(HttpStatus.OK, "USER2001", "내 정보 조회에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  UserSuccessCode(HttpStatus httpStatus, String code, String message) {
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
