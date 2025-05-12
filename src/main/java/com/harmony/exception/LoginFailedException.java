package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class AuthFailedException extends BusinessException {
  public AuthFailedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
