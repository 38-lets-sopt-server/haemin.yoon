package org.sopt.global.exception.code;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements BaseErrorCode {

  COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5001", "서버 에러가 발생했습니다."),
  COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON4001", "잘못된 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  GlobalErrorCode(HttpStatus httpStatus, String code, String message) {
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