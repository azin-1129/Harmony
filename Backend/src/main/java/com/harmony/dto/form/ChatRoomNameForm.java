package com.harmony.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomNameForm {
  @Schema(description = "이름을 변경하려는 채팅방 ID", defaultValue = "1")
  private Long chatRoomId;

  // FE에서 기존 채팅방 이름과 같지 않을 경우에만 입력하도록 설정, 공백 시 BE로 요청을 보내지 않음
  @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]{1,30}$", message = "채팅방 이름은 영문 대소문자,한글, 숫자, 공백 1~30자리여야 합니다.")
  @Schema(description = "새 채팅방 이름", defaultValue = "러닝머신 고장내기 모임")
  private String newChatRoomName;
}
