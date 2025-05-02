package com.harmony.security.filter;

import com.harmony.exception.ExpiredTokenException;
import com.harmony.global.response.code.ErrorCode;
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

    log.info("Authentication Filter");
    if (StringUtils.isNotBlank(jwt)) {
      if (tokenUtils.isValidToken(jwt)) {
        Authentication authentication = tokenUtils.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken=request.getHeader(AUTHORIZATION_HEADER);
    if(StringUtils.isNotBlank(bearerToken)){
      if(bearerToken.startsWith(BEARER_PREFIX) && bearerToken.length()>7){
        return bearerToken.substring(7);
      }
    }
    return null; // TODO: null 반환해도 되나?
  }
}
