package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class ExpiredTokenException extends BusinessException {
  public ExpiredTokenException(ErrorCode errorCode) {
    super(errorCode);
  }
}
