package org.sopt.domain.like.exception.code;

import org.sopt.global.exception.code.BaseSuccessCode;
import org.springframework.http.HttpStatus;

public enum LikeSuccessCode implements BaseSuccessCode {

  LIKE_SUCCESS(HttpStatus.CREATED, "LIKE2011", "좋아요를 눌렀습니다."),
  LIKE_CANCEL_SUCCESS(HttpStatus.OK, "LIKE2001", "좋아요를 취소했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  LikeSuccessCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  @Override
  public HttpStatus getHttpStatus() { return httpStatus; }

  @Override
  public String getCode() { return code; }

  @Override
  public String getMessage() { return message; }
}
