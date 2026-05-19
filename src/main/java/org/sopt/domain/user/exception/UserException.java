package org.sopt.domain.user.exception;

import org.sopt.global.exception.CustomException;
import org.sopt.global.exception.code.BaseErrorCode;

public class UserException extends CustomException {

  public UserException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
