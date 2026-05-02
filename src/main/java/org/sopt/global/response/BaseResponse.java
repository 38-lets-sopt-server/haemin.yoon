package org.sopt.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.global.exception.code.BaseSuccessCode;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"}) // JSON 응답 필드 출력 순서를 고정
public class BaseResponse<T> {

  @Schema(description = "성공 여부")
  private final Boolean isSuccess;

  @Schema(description = "응답 상태 코드")
  private final String code;

  @Schema(description = "응답 메시지")
  private final String message;

  @Schema(description = "응답 결과 (실패 시 null)")
  private T result;

  public BaseResponse(Boolean isSuccess, String code, String message, T result) {
    this.isSuccess = isSuccess;
    this.code = code;
    this.message = message;
    this.result = result;
  }

  // 성공 응답 — BaseSuccessCode를 구현한 Enum이라면 모두 받을 수 있다
  public static <T> BaseResponse<T> onSuccess(BaseSuccessCode successCode, T result) {
    return new BaseResponse<>(true,
        successCode.getCode(),
        successCode.getMessage(),
        result);
  }

  // 실패 응답
  public static <T> BaseResponse<T> onFailure(String code, String message) {
    return new BaseResponse<>(false, code, message, null);
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
