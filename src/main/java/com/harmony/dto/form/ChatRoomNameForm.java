package com.harmony.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "이름을 변경하려는 채팅방ID", defaultValue = "1")
  private Long chatRoomId;

  // FE에서 기존 채팅방 이름과 같지 않을 경우에만 입력하도록 설정
  @Schema(description = "새 채팅방 이름", defaultValue = "러닝머신 고장내기 모임")
  private String newChatRoomName;
}
