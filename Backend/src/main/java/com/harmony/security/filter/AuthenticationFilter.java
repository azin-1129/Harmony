package com.harmony.security.filter;

import com.harmony.security.utils.TokenProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
  public static final String AUTHORIZATION_HEADER="Authorization";
  public static final String BEARER_PREFIX="Bearer";

  private final TokenProvider tokenUtils;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {
    // Request header에서 token 추출
    String jwt=resolveToken(request);

    log.info("request URL:"+request.getRequestURL());
    log.info("jwt token:"+jwt);
    if (StringUtils.isNotBlank(jwt)) { // Token Provider에 넣으면 permit 경로 통과 시 exception 남
      if (tokenUtils.isValidToken(jwt)) {
        Authentication authentication = tokenUtils.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("JWT 인증을 완료하여, SecurityContext에 정보를 등록했습니다.");
      }
    }else{
      log.info("Token이 비어 인증을 진행하지 않습니다.");
    }

    filterChain.doFilter(request, response);
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
