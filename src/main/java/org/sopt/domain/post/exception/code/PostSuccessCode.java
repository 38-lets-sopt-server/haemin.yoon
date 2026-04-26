package org.sopt.domain.post.exception.code;

import org.sopt.global.exception.code.BaseSuccessCode;
import org.springframework.http.HttpStatus;

public enum PostSuccessCode implements BaseSuccessCode {

  POST_CREATE_SUCCESS(HttpStatus.CREATED, "POST2011", "게시글 작성에 성공했습니다."),
  POST_GET_ALL_SUCCESS(HttpStatus.OK, "POST2001", "게시글 전체 조회에 성공했습니다."),
  POST_GET_SUCCESS(HttpStatus.OK, "POST2002", "게시글 단건 조회에 성공했습니다."),
  POST_UPDATE_SUCCESS(HttpStatus.OK, "POST2003", "게시글 수정에 성공했습니다."),
  POST_DELETE_SUCCESS(HttpStatus.OK, "POST2004", "게시글 삭제에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  PostSuccessCode(HttpStatus httpStatus, String code, String message) {
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
