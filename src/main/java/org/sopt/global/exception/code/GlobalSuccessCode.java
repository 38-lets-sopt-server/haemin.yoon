package org.sopt.global.exception.code;

import org.springframework.http.HttpStatus;

public enum GlobalSuccessCode implements BaseSuccessCode {

  COMMON_OK(HttpStatus.OK, "COMMON2001", "요청에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  GlobalSuccessCode(HttpStatus httpStatus, String code, String message) {
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