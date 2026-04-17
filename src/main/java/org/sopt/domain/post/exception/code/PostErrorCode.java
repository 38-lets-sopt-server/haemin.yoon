package org.sopt.domain.post.exception.code;


import org.sopt.global.exception.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PostErrorCode implements BaseErrorCode {

  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4041", "해당 게시글을 찾을 수 없습니다."),
  POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "POST4001", "잘못된 게시글 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  PostErrorCode(HttpStatus httpStatus, String code, String message) {
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
