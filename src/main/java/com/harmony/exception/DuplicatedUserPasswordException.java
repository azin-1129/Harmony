package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class DuplicatedUserPasswordException extends BusinessException{
  public DuplicatedUserPasswordException(ErrorCode errorCode) {
    super(errorCode);
  }
}
