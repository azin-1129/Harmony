package com.harmony.controller;

import com.harmony.dto.form.NicknameForm;
import com.harmony.dto.form.PasswordForm;
import com.harmony.dto.form.ProfileImageForm;
import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value="/user")
@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserService userService;
  private final Long givenUserId=1L; // 임시 userId

  // TODO: userId 처리, PasswordForm validation
  @PostMapping("/register")
  public ResponseEntity<Object> registerUser(@RequestBody RegisterRequestDto registerRequestDto){
    userService.registerUser(registerRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_REGISTER_SUCCESS
    );
  }

  // 비번 업뎃
  @PostMapping("update/password")
  public ResponseEntity<Object> updateUserPassword(@RequestBody PasswordForm passwordForm){
    userService.updateUserPassword(givenUserId, passwordForm.getNewPassword());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_PASSWORD_SUCCESS
    );
  }

  // 닉넴 업뎃
  @PostMapping("update/nickname")
  public ResponseEntity<Object> updateUserNickname(@RequestBody NicknameForm nicknameForm){
    userService.updateUserNickname(givenUserId, nicknameForm.getNewNickname());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_NICKNAME_SUCCESS
    );
  }

  // 프사 업뎃
  @PostMapping("update/profile-image")
  public ResponseEntity<Object> updateUserProfileImageName(@RequestBody ProfileImageForm profileImageForm){
    userService.updateUserProfileImage(givenUserId, profileImageForm.getNewProfileImageName());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_PROFILE_IMAGE_SUCCESS
    );
  }

  // 탈퇴
  @DeleteMapping("/{userId}")
  public ResponseEntity<Object> deleteUser(@PathVariable Long userId){ // Security에서 빼올 예정
    userService.deleteUser(userId);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_WITHDRAW_SUCCESS
    );
  }
}
