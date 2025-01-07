package com.harmony.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {
  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡUserㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE
  INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "양식이 잘못되었습니다."),
  USER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입한 회원입니다."),

  // READ

  // UPDATE

  // DELETE
  USER_ALREADY_WITHDRAW(HttpStatus.NOT_FOUND,"이미 탈퇴한 회원입니다.");

  private final HttpStatus httpStatus;

  private final String message;
}
