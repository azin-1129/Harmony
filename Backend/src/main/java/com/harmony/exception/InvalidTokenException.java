package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
  public InvalidTokenException(ErrorCode errorCode) {
    super(errorCode);
  }
}
