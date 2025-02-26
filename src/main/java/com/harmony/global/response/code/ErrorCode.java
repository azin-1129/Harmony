package com.harmony.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {
  INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "양식이 잘못되었습니다."),

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡUserㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE
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
  CHATROOM_FULL(HttpStatus.BAD_REQUEST, "대화상대가 많아 참여할 수 없습니다. 잠시 후 다시 시도해 주세요."),
  // READ

  // UPDATE

  // DELETE
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡParticipantㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  PARTICIPANT_ALREADY_LEFT(HttpStatus.BAD_REQUEST, "이미 퇴장한 참가자입니다."),

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡFriendshipRequestㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE
  FRIENDSHIP_REQUEST_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 진행중인 친구 요청입니다."),
  // READ

  // UPDATE
  INVALID_FRIENDSHIP_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 친구 요청입니다.");

  // DELETE

  private final HttpStatus httpStatus;

  private final String message;
}
