package org.sopt.dto;

public class ApiResponse<T> {
  public boolean success; // 요청이 성공했는지 실패했는지 나타내는 값
  public T data; // 실제 응답 데이터. T 자리에 어떤 타입이 들어오느냐에 따라 이 data의 타입도 달라진다.
  public String message;

  // 생성자
  // success, data, message 값을 받아서 ApiResponse 객체를 생성
  public ApiResponse(boolean success, T data, String message) {
    this.success = success;
    this.data = data;
    this.message = message;
  }

  // 성공 응답을 쉽게 만들기 위한 정적 메서드 (사용 예: return ApiResponse.success(post, "게시글 조회 성공");)
  // success = true, data = 전달받은 실제 데이터, message = 전달받은 메시지
  // 를 넣어서 ApiResponse 객체를 만들어 반환한다.
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, data, message);
  }

  // 실패 응답을 쉽게 만들기 위한 정적 메서드
  public static <T> ApiResponse<T> fail(String message) {
    return new ApiResponse<>(false, null, message);
  }
}