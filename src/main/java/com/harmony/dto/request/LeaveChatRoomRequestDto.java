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
public class LeaveChatRoomRequestDto {
  @Schema(description = "스스로 퇴장할 채팅방 ID", defaultValue="3")
  private Long chatRoomId;
}