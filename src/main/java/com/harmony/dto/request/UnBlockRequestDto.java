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
public class UnBlockRequestDto {
  @Schema(description = "차단 해제할 상대방의 Identifier", defaultValue="pikmin")
  private String unBlockedUserIdentifier;
}
