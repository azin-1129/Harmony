package com.harmony.global.response.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.ResponseCode;
import com.harmony.global.response.code.SuccessCode;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {
  private final String code;

  private final String message;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private final Map<String, String> errors;

  public static <T> ResponseEntity<Object> createError(final ErrorCode errorCode, final T data) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(SuccessResponse.builder()
            .code(errorCode.name())
            .message(errorCode.getMessage())
            .data(data)
            .build()
        );
  }

  public static <T> ResponseEntity<Object> createError(final ErrorCode errorCode) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(SuccessResponse.builder()
            .code(errorCode.name())
            .message(errorCode.getMessage())
            .build()
        );
  }

  public static ErrorResponse of(final ResponseCode responseCode) {
    return ErrorResponse.builder()
        .code(responseCode.name())
        .message(responseCode.getMessage())
        .build();
  }

  public static ErrorResponse of(final ResponseCode responseCode, final String message) {
    return ErrorResponse.builder()
        .code(responseCode.name())
        .message(message)
        .build();
  }

  public static ErrorResponse of(final BindException e, final ResponseCode responseCode) {
    final Map<String, String> errors = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("")
        ));

    return ErrorResponse.builder()
        .code(responseCode.name())
        .message(responseCode.getMessage())
        .build();
  }

  public static ErrorResponse of(final ConstraintViolationException e, final ResponseCode responseCode) {
    final Map<String, String> errors = e.getConstraintViolations()
        .stream()
        .collect(Collectors.toMap(
            violation -> StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                .reduce((first, second) -> second)
                .map(Object::toString)
                .orElse(""),
            ConstraintViolation::getMessage
        ));

    return ErrorResponse.builder()
        .code(responseCode.name())
        .message(responseCode.getMessage())
        .errors(errors)
        .build();
  }
}
