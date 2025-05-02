package com.harmony.dto.request;

import com.harmony.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterRequestDto {
  // TODO: 정규식 수정
  @NotBlank(message="이메일을 입력하세요.")
  @Email
  @Schema(description = "이메일", defaultValue = "azin1129@naver.com")
  private String email;

  @NotBlank(message="식별자를 입력하세요.")
  @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "식별자는 영문 소문자 또는 숫자 4~20자리여야 합니다.")
  @Schema(description = "회원 식별자 영문 소문자 또는 숫자 4~20자리", defaultValue = "choco")
  private String userIdentifier;

  @NotBlank(message="닉네임을 입력하세요.")
  @Pattern(regexp="^[가-힣]{2,10}$", message="닉네임은 한글 2~10자리여야 합니다.")
  @Schema(description = "닉네임 한글 2~10자리", defaultValue = "초코고양이")
  private String nickname;

  @NotBlank(message="비밀번호를 입력하세요.")
  @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
      message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8~20자의 비밀번호여야 합니다.")
  @Schema(description = "비밀번호 영어 대소문자+숫자+특수문자 8~20자", defaultValue = "azin1129!")
  private String password;

  @NotBlank(message="비밀번호 확인을 입력하세요.")
  @Schema(description = "비밀번호 확인", defaultValue = "azin1129!")
  private String passwordConfirm;

  @Schema(description = "권한", defaultValue = "ROLE_MEMBER", allowableValues = {"ROLE_MEMBER", "ROLE_ADMIN"})
  private Role role;
}
