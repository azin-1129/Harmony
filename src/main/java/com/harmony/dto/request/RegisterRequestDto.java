package com.harmony.dto.request;

import com.harmony.entity.Role;
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
  private String email;

  @NotBlank(message="식별자를 입력하세요.")
  @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "식별자는 영문 소문자와 숫자 4~20자리여야 합니다.")
  private String userIdentifier;

  // TODO: 닉네임 추가

  @NotBlank(message="비밀번호를 입력하세요.")
  @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
      message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8~20자의 비밀번호여야 합니다.")
  private String password;

  // TODO: password Confirm

  private Role role;
}
