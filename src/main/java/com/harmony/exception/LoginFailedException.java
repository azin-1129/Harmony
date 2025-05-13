package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class LoginFailedException extends BusinessException {
  public LoginFailedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
