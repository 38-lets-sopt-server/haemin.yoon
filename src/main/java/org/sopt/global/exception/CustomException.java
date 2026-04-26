package org.sopt.global.exception;

import org.sopt.global.exception.code.BaseErrorCode;

public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BaseErrorCode getErrorCode() {
    return errorCode;
  }
}
