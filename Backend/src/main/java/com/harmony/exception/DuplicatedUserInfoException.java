package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class DuplicatedUserInfoException extends BusinessException {
  public DuplicatedUserInfoException(ErrorCode errorCode) {
    super(errorCode);
  }
}
