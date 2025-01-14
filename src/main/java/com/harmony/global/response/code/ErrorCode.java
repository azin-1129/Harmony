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
  DUPLICATED_USER_PASSWORD(HttpStatus.BAD_REQUEST, "이미 사용하고 있는 비밀번호입니다."),
  DUPLICATED_USER_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용하고 있는 닉네임입니다."),

  // DELETE
  USER_ALREADY_WITHDRAW(HttpStatus.NOT_FOUND,"이미 탈퇴한 회원입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡChatRoomㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE

  // READ

  // UPDATE

  // DELETE
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다.");

  private final HttpStatus httpStatus;

  private final String message;
}
