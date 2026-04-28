package com.harmony.security.handler;

import com.harmony.entity.RefreshToken;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.RefreshTokenRepository;
import com.harmony.security.utils.TokenProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogoutHandlerImpl implements LogoutHandler {
  public static final String AUTHORIZATION_HEADER="Authorization";
  public static final String BEARER_PREFIX="Bearer";

  private final TokenProvider tokenUtils;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
    // Access Token이랑 Refresh Token을 어떻게 무효화하면 좋을까?
    // Access Token: 이건 짧게 가져가는 수밖에 없지 않나? 무효화하는 게 좋을 듯.
    // Refresh Token: Repository에서 삭제. (특정 대체값이 있다면 곤란할 것)
    System.out.println("로그아웃을 시도하고 있슨");
    // Request header에서 token 추출
    String accessToken =resolveToken(request);

    log.info("request URL:"+request.getRequestURL());
    log.info("jwt token:"+ accessToken);
    log.info("Authentication Filter");
    if (StringUtils.isNotBlank(accessToken)) {
      if (tokenUtils.isValidToken(accessToken)) {
        String email=tokenUtils.getUsername(accessToken);
       log.info("email은"+email);
       RefreshToken refreshToken=refreshTokenRepository.findByEmail(email)
           .orElseThrow(() -> new EntityNotFoundException(
               ErrorCode.AUTH_INVALID_TOKEN
           ));
       refreshTokenRepository.delete(refreshToken);
      }
    }else{
      log.warn("JWT 인증이 정상적으로 수행되지 않았습니다.");
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken=request.getHeader(AUTHORIZATION_HEADER);
    log.debug("bearer token:"+bearerToken);
    if(StringUtils.isNotBlank(bearerToken)){
      if(bearerToken.startsWith(BEARER_PREFIX) && bearerToken.length()>7){
        return bearerToken.substring(7);
      }
    }
    return null;
  }
}
