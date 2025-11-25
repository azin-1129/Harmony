package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class DuplicatedUserNicknameException extends BusinessException {
  public DuplicatedUserNicknameException(ErrorCode errorCode) {
    super(errorCode);
  }
}
