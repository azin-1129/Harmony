package com.harmony.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImageForm {
  @Schema(description = "새 프로필 사진", defaultValue = "kitty-zini.png")
  private String newProfileImageName;
}
