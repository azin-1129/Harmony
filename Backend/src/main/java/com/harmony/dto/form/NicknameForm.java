package com.harmony.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
public class NicknameForm {
  @Pattern(regexp="^[가-힣]{2,10}$", message="닉네임은 한글 2~10자리여야 합니다.")
  @Schema(description = "새 닉네임", defaultValue = "치즈고양이")
  private String newNickname;
}
