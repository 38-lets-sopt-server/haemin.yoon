package org.sopt.global.exception;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.sopt.global.exception.code.BaseErrorCode;
import org.sopt.global.response.BaseResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // CustomException 처리 — UserException, PostException 등 모두 여기서 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Void>> handleCustomException(CustomException e) {
    BaseErrorCode errorCode = e.getErrorCode();
    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
  }

  // 그 외 예상치 못한 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
    return ResponseEntity
        .status(500)
        .body(BaseResponse.onFailure("COMMON5001", "서버 에러가 발생했습니다."));
  }

  // @RequestParam, @PathVariable 등에 붙은 제약 어노테이션 위반 시 발생
  // @RequestBody의 @Valid → MethodArgumentNotValidException과 다른 예외임에 주의
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
    String message = e.getConstraintViolations().iterator().next().getMessage();
    BaseErrorCode errorCode = findErrorCodeByMessage(message);

    if (errorCode != null) {
      return ResponseEntity
          .status(errorCode.getHttpStatus())
          .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseResponse.onFailure("COMMON4001", message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    // 1. DTO에서 터진 에러 메시지 추출
    String defaultMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

    // 2. 모든 에러 코드(PostErrorCode, UserErrorCode 등)의 부모인 BaseErrorCode를 활용해 매칭
    // PostErrorCode.values() 등을 활용해 메시지가 일치하는 Enum을 찾습니다.
    BaseErrorCode errorCode = findErrorCodeByMessage(defaultMessage);

    if (errorCode != null) {
      return ResponseEntity
          .status(errorCode.getHttpStatus())
          .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    // 3. 만약 매칭되는 커스텀 코드가 없다면 기본 에러로 응답
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseResponse.onFailure("COMMON4001", defaultMessage));
  }

  private final Map<String, BaseErrorCode> errorCodeMap = new HashMap<>();

  // 애플리케이션 시작 시 org.sopt 하위의 BaseErrorCode 구현 enum을 자동 탐색해 맵에 등록
  // 새 도메인 ErrorCode를 추가해도 이 파일을 수정할 필요 없음
  @PostConstruct
  public void initErrorCodeMap() {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(BaseErrorCode.class));

    for (BeanDefinition bd : scanner.findCandidateComponents("org.sopt")) {
      try {
        Class<?> clazz = Class.forName(bd.getBeanClassName());
        if (clazz.isEnum()) {
          for (Object constant : clazz.getEnumConstants()) {
            BaseErrorCode code = (BaseErrorCode) constant;
            errorCodeMap.put(code.getMessage(), code);
          }
        }
      } catch (ClassNotFoundException ignored) {}
    }
  }

  private BaseErrorCode findErrorCodeByMessage(String message) {
    return errorCodeMap.get(message);
  }
}
