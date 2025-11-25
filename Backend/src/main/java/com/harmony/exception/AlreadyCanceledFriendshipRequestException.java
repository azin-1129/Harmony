package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class AlreadyCanceledFriendshipRequestException extends BusinessException {
  public AlreadyCanceledFriendshipRequestException(ErrorCode errorCode) {
    super(errorCode);
  }
}