package org.sopt.domain.post.exception;

import org.sopt.global.exception.CustomException;
import org.sopt.global.exception.code.BaseErrorCode;

public class PostException extends CustomException {

  public PostException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
