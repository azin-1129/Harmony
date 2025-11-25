package com.harmony.global.response.exception;

import com.harmony.global.response.code.ErrorCode;

public class UploadFailedException extends BusinessException{
  public UploadFailedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
