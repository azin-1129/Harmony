package com.harmony.global.response.exception;

import com.harmony.global.response.code.ErrorCode;

public class EntityNotFoundException extends BusinessException{
  public EntityNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
