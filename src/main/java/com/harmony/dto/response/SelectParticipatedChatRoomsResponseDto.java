package com.harmony.dto.response;

import com.harmony.entity.ChatRoomType;
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
public class SelectParticipatedChatRoomsResponseDto {
  private Long chatRoomId;
  private String chatRoomName;
  private Integer chatRoomCount;
  private Integer chatRoomCountMax;
  private ChatRoomType chatRoomType;
}
