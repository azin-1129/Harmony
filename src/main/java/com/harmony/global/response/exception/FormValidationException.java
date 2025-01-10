package com.harmony.global.response.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.ResponseCode;
import java.util.HashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FormValidationException extends RuntimeException {
  private final ErrorCode errorCode;
  private final HashMap<String, String> validationErrors;
}
