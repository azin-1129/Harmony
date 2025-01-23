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
public class CreateAndParticipatePersonalChatRoomRequestDto {
  @Schema(description="개인 채팅방을 생성할 상대방 Identifier", defaultValue="cheese")
  private String partnerIdentifier;
}
