package com.harmony.global.response.handler;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.ResponseCode;
import com.harmony.global.response.exception.BusinessException;
import com.harmony.global.response.exception.FormValidationException;
import com.harmony.global.response.structure.ErrorResponse;
import com.harmony.global.response.structure.SuccessResponse;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations={RestController.class}, basePackages={"com.harmony.controller"})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Object> handleRestApiException(final BusinessException e){
    logger.error("RestApiException caught: ", e);

    final ResponseCode responseCode=e.getResponseCode();
    return handleExceptionInternal(responseCode);
  }

  @ExceptionHandler(FormValidationException.class)
  public ResponseEntity<Object> handleRestApiExceptionReturnsErrorData(final FormValidationException e){
    logger.error("RestApiException caught: ", e);

    final ErrorCode errorCode=e.getErrorCode();
    final HashMap<String, String> validationErrors=e.getValidationErrors();

    return handleExceptionInternalImportErrorData(errorCode, validationErrors);
  }

  private ResponseEntity<Object> handleExceptionInternal(final ResponseCode responseCode) {
    return ResponseEntity.status(responseCode.getHttpStatus())
        .body(ErrorResponse.of(responseCode));
  }

  private ResponseEntity<Object> handleExceptionInternalImportErrorData(final ResponseCode responseCode, final HashMap<String, String> data) {
    return ResponseEntity.status(responseCode.getHttpStatus())
        .body(ErrorResponse.builder()
            .code(responseCode.name())
            .message(responseCode.getMessage())
            .errors(data)
            .build()
        );
  }
}
