package com.harmony.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.structure.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    log.info("접근 권한이 없습니다.");
    setErrorResponse(response, ErrorCode.AUTH_AUTHORIZE_FAILED);
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
