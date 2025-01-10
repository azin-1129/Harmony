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
public class PasswordForm {
  @Schema(description = "새 비밀번호", defaultValue = "azin1129?")
  private String newPassword;
  @Schema(description = "새 비밀번호 확인", defaultValue = "azin1129?")
  private String newPasswordConfirm;
}
