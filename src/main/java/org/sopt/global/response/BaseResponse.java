package org.sopt.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.sopt.global.exception.code.BaseSuccessCode;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"}) // JSON 응답 필드 출력 순서를 고정
public class ApiResponse<T> {

  private final Boolean isSuccess;
  private final String code;
  private final String message;
  private T result;

  public ApiResponse(Boolean isSuccess, String code, String message, T result) {
    this.isSuccess = isSuccess;
    this.code = code;
    this.message = message;
    this.result = result;
  }

  // 성공 응답 — BaseSuccessCode를 구현한 Enum이라면 모두 받을 수 있다
  public static <T> ApiResponse<T> onSuccess(BaseSuccessCode successCode, T result) {
    return new ApiResponse<>(true,
        successCode.getCode(),
        successCode.getMessage(),
        result);
  }

  // 실패 응답
  public static <T> ApiResponse<T> onFailure(String code, String message) {
    return new ApiResponse<>(false, code, message, null);
  }

  public Boolean getSuccess() {
    return isSuccess;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public T getResult() {
    return result;
  }
}
