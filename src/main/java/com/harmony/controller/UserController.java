package com.harmony.controller;

import com.harmony.dto.form.NicknameForm;
import com.harmony.dto.form.PasswordForm;
import com.harmony.dto.form.ProfileImageForm;
import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.ErrorResponse;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.UserService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
  public ResponseEntity<Object> registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto, BindingResult bindingResult){
    // 입력 형식이 올바르지 않다면 에러
    if(bindingResult.hasErrors()){
      Map<String, String> validatorResult=new HashMap<>();

      for(FieldError error:bindingResult.getFieldErrors()){
        String validKeyName=String.format("valid_%s",error.getField());
        validatorResult.put(validKeyName, error.getDefaultMessage());
      }

      return ErrorResponse.createError(
          ErrorCode.INVALID_ARGUMENT,
          validatorResult
      );
    }
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
