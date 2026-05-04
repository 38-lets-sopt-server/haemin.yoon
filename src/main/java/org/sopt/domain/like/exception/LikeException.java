package org.sopt.domain.like.exception;

import org.sopt.global.exception.CustomException;
import org.sopt.global.exception.code.BaseErrorCode;

public class LikeException extends CustomException {

  public LikeException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
