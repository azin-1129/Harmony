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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="회원가입 성공"),
      @ApiResponse(responseCode="400", description="입력 양식이 잘못되었습니다.")
  })
  public ResponseEntity<Object> registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto, BindingResult bindingResult){
    log.info("user Controller 진입");
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

  @GetMapping("/info")
  @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN')")
  public ResponseEntity<Object> selectUserInfo(){
    return SuccessResponse.createSuccess(
        SuccessCode.USER_INFO_READ_SUCCESS,
        userService.findUserInfo()
    );
  }
  // 비번 업뎃
  @PostMapping("/update/password")
  @Operation(summary="비밀번호 변경", description="비밀번호 변경 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="비밀번호 변경 성공"),
      @ApiResponse(responseCode="400", description="입력 양식이 잘못되었습니다.")
  })
  public ResponseEntity<Object> updateUserPassword(@RequestBody @Valid PasswordForm passwordForm, BindingResult bindingResult){
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
  @PostMapping("/update/nickname")
  @Operation(summary="닉네임 변경", description="닉네임 변경 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="닉네임 변경 성공"),
      @ApiResponse(responseCode="400", description="입력 양식이 잘못되었습니다.")
  })
  public ResponseEntity<Object> updateUserNickname(@RequestBody @Valid NicknameForm nicknameForm, BindingResult bindingResult){
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

    userService.updateUserNickname(givenUserId, nicknameForm.getNewNickname());

    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_NICKNAME_SUCCESS
    );
  }

  // 프사 업뎃
  // TODO: 지원하지 않는 이미지 형식?
  // TODO: 이미지 @Valid도 있나..? Dto에는 없음
  @PostMapping("/update/profile-image")
  @Operation(summary="프로필 사진 변경", description="프로필 사진 변경 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="프로필 사진 변경 성공"),
      @ApiResponse(responseCode="400", description="입력 양식이 잘못되었습니다.")
  })
  public ResponseEntity<Object> updateUserProfileImageName(@RequestBody @Valid ProfileImageForm profileImageForm){
    userService.updateUserProfileImage(givenUserId, profileImageForm.getNewProfileImageName());

    // TODO: S3에 이미지 저장
    return SuccessResponse.createSuccess(
        SuccessCode.USER_UPDATE_PROFILE_IMAGE_SUCCESS
    );
  }

  // 탈퇴
  @DeleteMapping
  @Operation(summary="회원탈퇴", description="회원탈퇴 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="회원탈퇴 성공"),
      @ApiResponse(responseCode="404", description="회원탈퇴 실패")
  })
  public ResponseEntity<Object> deleteUser(){ // Security에서 빼올 예정
    userService.deleteUser(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_WITHDRAW_SUCCESS
    );
  }

  @DeleteMapping("/force/{userId}")
  @Operation(summary="테스트용 회원탈퇴", description="회원탈퇴 API")
  public ResponseEntity<Object> deleteUserForce(@PathVariable Long userId){ // Security에서 빼올 예정
    userService.deleteUserForce(userId);

    return SuccessResponse.createSuccess(
        SuccessCode.USER_WITHDRAW_SUCCESS
    );
  }
}
