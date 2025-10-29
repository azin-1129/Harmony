package com.harmony.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class LogoutHandlerImpl implements LogoutHandler {
  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
    // Access Token이랑 Refresh Token을 어떻게 무효화하면 좋을까?

    System.out.println("로그아웃을 시도하고 있슨");
    // Access Token: 이건 짧게 가져가는 수밖에 없지 않나? 무효화하는 게 좋을 듯.
    // Refresh Token: Repository에서 무효화
  }
}
