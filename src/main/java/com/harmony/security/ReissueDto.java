package com.harmony.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReissueDto {
  private String accessToken;
  private String refreshToken; // TODO: frontend에서 이걸 가지고 있어야 하는가?
}
