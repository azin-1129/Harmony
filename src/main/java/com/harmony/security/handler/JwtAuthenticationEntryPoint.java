package com.harmony.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException{
    log.info("Authentication Entry Point");
    log.info(request.getRequestURI());
    // 유효한 자격 증명을 제공하지 않고 접근 시 401
    log.error("유효한 자격이 아니에요. 🙄");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
