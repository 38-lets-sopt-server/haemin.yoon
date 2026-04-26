package org.sopt.domain.post.exception.code;


import org.sopt.global.exception.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PostErrorCode implements BaseErrorCode {

  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4041", "해당 게시글을 찾을 수 없습니다."),
  POST_BAD_REQUEST(HttpStatus.BAD_REQUEST, "POST4000", "잘못된 게시글 요청입니다."),
  POST_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "POST4001", "제목은 필수 입력 항목입니다."),
  POST_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "POST4002", "내용을 입력해주세요."),
  POST_AUTHOR_REQUIRED(HttpStatus.BAD_REQUEST, "POST4003", "작성자는 필수입니다."),
  POST_BOARD_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "POST4004", "게시판 종류(boardType)는 필수입니다."),
  POST_INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "POST4005", "page는 0 이상, size는 1 이상이어야 합니다.");
  //POST_INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "POST4002", "page는 0 이상, size는 1 이상이어야 합니다.");

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
