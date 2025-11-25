package com.harmony.controller;

import com.harmony.dto.request.LoginDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.security.ReissueDto;
import com.harmony.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<Object> login(@Valid @RequestBody LoginDto loginDto){
    return SuccessResponse.createSuccess(
        SuccessCode.AUTH_LOGIN_SUCCESS,
        authService.login(loginDto)
    );
  }

  @PostMapping("/reissue")
  public ResponseEntity<Object> reissue(@RequestBody ReissueDto reissueDto){
    return SuccessResponse.createSuccess(
        SuccessCode.AUTH_REISSUE_SUCCESS,
        authService.reissue(reissueDto)
    );
  }
}
