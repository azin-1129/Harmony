package com.harmony.dto.response;

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
public class SelectProfileResponseDto {
  private String profileImageUrl;
  private String nickname;
  private String userIdentifier;
  // TODO: 친구 카운트
}
