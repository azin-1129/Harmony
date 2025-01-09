package com.harmony.global.response.exception;

import com.harmony.global.response.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {
  private final ResponseCode responseCode;
}
