package com.harmony.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class LoginDto {
  @NotBlank
  @Email
  @Schema(description="로그인할 회원의 email", defaultValue = "azin1129@naver.com")
  private String email;

  @NotBlank
  @Schema(description="로그인할 회원의 PW", defaultValue = "azin1129!")
  private String password;
}
