package com.harmony.dto.request;

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
public class LeaveChatRoomForceRequestDto {
  @Schema(description = "강퇴할 참가자가 있는 채팅방 ID", defaultValue = "2")
  private Long chatRoomId;
  @Schema(description = "강퇴할 참가자의 Identifier", defaultValue="cheese")
  private String userIdentifier;
}
