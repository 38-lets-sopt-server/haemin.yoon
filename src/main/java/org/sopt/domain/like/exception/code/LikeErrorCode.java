package org.sopt.domain.like.exception.code;

import org.sopt.global.exception.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum LikeErrorCode implements BaseErrorCode {

  LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "LIKE4091", "이미 좋아요를 누른 게시글입니다."),
  LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE4041", "좋아요를 누르지 않은 게시글입니다."),
  LIKE_USER_REQUIRED(HttpStatus.BAD_REQUEST, "LIKE4001", "사용자 ID는 필수입니다."),
  // 재시도 횟수를 모두 소진했을 때 최종 응답 — 클라이언트에게 재시도 안내
  LIKE_CONCURRENT_CONFLICT(HttpStatus.CONFLICT, "LIKE4092", "요청이 집중되고 있습니다. 잠시 후 다시 시도해주세요.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  LikeErrorCode(HttpStatus httpStatus, String code, String message) {
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
