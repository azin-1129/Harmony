package com.harmony.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {
  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡUserㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE
  USER_REGISTER_SUCCESS(HttpStatus.CREATED, "회원가입에 성공했습니다."),

  // READ

  // UPDATE
  USER_UPDATE_PASSWORD_SUCCESS(HttpStatus.OK, "비밀번호를 변경했습니다."),
  USER_UPDATE_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임을 변경했습니다."),
  USER_UPDATE_PROFILE_IMAGE_SUCCESS(HttpStatus.OK, "프로필 사진을 변경했습니다."),

  // DELETE
  USER_WITHDRAW_SUCCESS(HttpStatus.OK, "탈퇴 완료되었습니다."),

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡChatRoomㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
  // CREATE
  PERSONAL_CHATROOM_CREATE_SUCCESS(HttpStatus.CREATED, "개인 채팅방 생성에 성공했습니다."),
  GROUP_CHATROOM_CREATE_SUCCESS(HttpStatus.CREATED, "단체 채팅방 생성에 성공했습니다."),

  // READ

  // UPDATE
  CHATROOM_NAME_UPDATE_SUCCESS(HttpStatus.OK, "채팅방 이름을 변경했습니다."),

  // DELETE
  CHATROOM_DELETE_SUCCESS(HttpStatus.OK, "채팅방 삭제 완료되었습니다.");

  private final HttpStatus httpStatus;

  private final String message;
}
