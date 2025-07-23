package com.harmony.controller;

import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.security.impl.UserDetailsImpl;
import com.harmony.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class AwsS3Controller {
  private final AwsS3Service awsS3Service;

  @PostMapping
  public ResponseEntity<Object> uploadProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, MultipartFile multipartFile){
    System.out.println("현재 로그인한 유저 정보입니당.");
    System.out.println(userDetailsImpl);
    return SuccessResponse.createSuccess(
        SuccessCode.PROFILE_IMAGE_UPLOAD_SUCCESS,
        awsS3Service.uploadProfileImage(userDetailsImpl, multipartFile)
    );
  }

  @DeleteMapping
  public ResponseEntity<Object> deleteProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
    awsS3Service.deleteProfileImage(userDetailsImpl.getProfileImageName());
    return SuccessResponse.createSuccess(
        SuccessCode.PROFILE_IMAGE_DELETE_SUCCESS
    );
  }
}
