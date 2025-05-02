package com.harmony.dto.response;

import com.harmony.entity.User;
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
public class SelectUserInfoResponseDto {
  // 이메일
  private String email;

  // 식별자
  private String userIdentifier;

  // 암호(?)-현재 pw, 바꿀 pw, 바꿀 pw 컨펌

  // 프사
  private String profileImageName;

  // 닉네임
  private String nickname;

}
