package com.harmony.security.handler;

import com.harmony.exception.AuthFailedException;
import com.harmony.exception.LoginFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.structure.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException{

    log.info("Authentication Entry Point");
    log.info(request.getRequestURI());
    // 유효한 자격 증명을 제공하지 않고 접근 시 401. 리디렉션 요청
    log.error("유효한 자격 증명이 아닙니다.");

    setErrorResponse(response, ErrorCode.AUTH_AUTHENTICATE_FAILED);
  }

  private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode){
    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    ErrorResponse errorResponse = ErrorResponse.of(errorCode);
    try {
      objectMapper.writeValue(response.getWriter(), errorResponse);
    }catch(IOException e){
      e.printStackTrace();
    }
  }
}
