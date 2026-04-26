package org.sopt.domain.post.exception;

import org.sopt.domain.post.exception.code.PostErrorCode;
import org.sopt.global.exception.CustomException;

public class PostNotFoundException extends CustomException {
  public PostNotFoundException() {
    super(PostErrorCode.POST_NOT_FOUND);
  }
}
