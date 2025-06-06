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
public class CancelSentFriendshipRequestRequestDto {
  @Schema(description = "취소할 친구 추가 요청의 수신자 Identifier", defaultValue="cheese")
  private String receiverIdentifier;
}
