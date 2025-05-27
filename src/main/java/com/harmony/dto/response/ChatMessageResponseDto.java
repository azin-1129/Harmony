package com.harmony.dto.response;

import com.harmony.entity.ChatMessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageResponseDto {
  private Long chatRoomId;
  private ChatMessageType chatMessageType;
  private Long senderId;
  private String chatMessage;
}
