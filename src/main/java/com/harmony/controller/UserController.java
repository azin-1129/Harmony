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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  // TODO: userId 처리, Password 암호화
  @PostMapping("/register")
  @Operation(summary="회원가입", description="회원가입 API")
  @Parameters({
      @Parameter(name="email", description="이메일", example="azin1129@naver.com"),
      @Parameter(name="userIdentifier", description="회원 식별자 영문 소문자 또는 숫자 4~20자리", example="choco"),
      @Parameter(name="nickname", description="닉네임 한글 2~10자리", example="초코고양이"),
      @Parameter(name="password", description="비밀번호 영어 대소문자+숫자+특수문자 8~20자", example="azin1129!"),
      @Parameter(name="passwordConfirm", description="비밀번호 확인", example="azin1129!"),
      @Parameter(name="role", description="권한", example="MEMBER"),
  })
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="회원가입 성공"),
      @ApiResponse(responseCode="400", description="입력 양식이 잘못되었습니다.")
  })
  public ResponseEntity<Object> registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto, BindingResult bindingResult){
    // TODO: Role 설정 어떻게 하지?

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

    // 비밀번호 불일치
    if(!registerRequestDto.getPassword().equals(registerRequestDto.getPasswordConfirm())){
      return ErrorResponse.createError(
          ErrorCode.INVALID_ARGUMENT
      );
    }

    userService.registerUser(registerRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_REGISTER_SUCCESS
    );
  }

  // 비번 업뎃
  @PostMapping("update/password")
  @Operation(summary="비밀번호 변경", description="비밀번호 변경 API")
  @Parameters({
      @Parameter(name = "newPassword", description = "새 비밀번호", example = "azin1129?"),
      @Parameter(name = "newPasswordConfirm", description = "새 비밀번호 확인", example = "azin1129?")
  })
  public ResponseEntity<Object> updateUserPassword(@RequestBody PasswordForm passwordForm){
    // 비밀번호 불일치
    if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
      return ErrorResponse.createError(
          ErrorCode.INVALID_ARGUMENT
      );
    }

    userService.updateUserPassword(givenUserId, passwordForm.getNewPassword());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_PASSWORD_SUCCESS
    );
  }

  // 닉넴 업뎃
  @PostMapping("update/nickname")
  @Operation(summary="닉네임 변경", description="닉네임 변경 API")
  @Parameters({
      @Parameter(name = "newNickname", description = "닉네임", example = "치즈고양이")
  })
  public ResponseEntity<Object> updateUserNickname(@RequestBody NicknameForm nicknameForm){
    userService.updateUserNickname(givenUserId, nicknameForm.getNewNickname());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_NICKNAME_SUCCESS
    );
  }

  // 프사 업뎃
  @PostMapping("update/profile-image")
  @Operation(summary="프로필 사진 변경", description="프로필 사진 변경 API")
  @Parameters({
      @Parameter(name = "newProfileImage", description = "프로필 이미지")
  })
  public ResponseEntity<Object> updateUserProfileImageName(@RequestBody ProfileImageForm profileImageForm){
    userService.updateUserProfileImage(givenUserId, profileImageForm.getNewProfileImageName());

    // TODO: S3에 이미지 저장
    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_PROFILE_IMAGE_SUCCESS
    );
  }

  // 탈퇴
  @DeleteMapping("/{userId}")
  @Operation(summary="회원탈퇴", description="회원탈퇴 API")
  @Parameters({
      @Parameter(name = "userId", description = "회원ID", example = "1")
  })
  public ResponseEntity<Object> deleteUser(@PathVariable Long userId){ // Security에서 빼올 예정
    userService.deleteUser(userId);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_WITHDRAW_SUCCESS
    );
  }
}
