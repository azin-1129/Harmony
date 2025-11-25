package com.harmony.dto.response;

import com.harmony.entity.ChatMessageType;
import java.time.LocalDateTime;
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
public class SelectChatRoomMessagesResponseDto {
  private Long chatMessageId;
  private ChatMessageType chatMessageType;
  private Long senderId;
  private String chatMessage;
  private LocalDateTime createdAt;
}
