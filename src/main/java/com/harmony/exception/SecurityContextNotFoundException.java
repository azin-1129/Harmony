package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class SecurityContextNotFoundException extends BusinessException {
  public SecurityContextNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
