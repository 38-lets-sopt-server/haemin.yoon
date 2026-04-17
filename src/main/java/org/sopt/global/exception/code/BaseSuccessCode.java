package org.sopt.global.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {
  HttpStatus getHttpStatus();
  String getCode();
  String getMessage();
}