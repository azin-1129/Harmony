package com.harmony.global.response.exception;

import com.harmony.global.response.code.ErrorCode;

public class EntityAlreadyExistException extends BusinessException{
  public EntityAlreadyExistException(ErrorCode errorCode) {
    super(errorCode);
  }
}
