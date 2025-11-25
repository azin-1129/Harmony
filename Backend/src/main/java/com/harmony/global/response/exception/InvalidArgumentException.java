package com.harmony.global.response.exception;

import com.harmony.global.response.code.ErrorCode;
import java.util.HashMap;

public class InvalidArgumentException extends FormValidationException {
  public InvalidArgumentException(ErrorCode errorCode, HashMap<String, String> validateResult) {
    super(errorCode, validateResult);
  }
}
