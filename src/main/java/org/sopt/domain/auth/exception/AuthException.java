package org.sopt.domain.auth.exception;

import org.sopt.global.exception.CustomException;
import org.sopt.global.exception.code.BaseErrorCode;

public class AuthException extends CustomException {

    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
