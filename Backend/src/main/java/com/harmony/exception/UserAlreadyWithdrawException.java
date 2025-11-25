package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class UserAlreadyWithdrawException extends BusinessException {
  public UserAlreadyWithdrawException(ErrorCode errorCode) {
    super(errorCode);
  }
}
