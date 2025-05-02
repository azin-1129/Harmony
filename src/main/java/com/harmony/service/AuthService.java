package com.harmony.service;

import com.harmony.dto.request.LoginDto;
import com.harmony.entity.RefreshToken;
import com.harmony.exception.AuthFailedException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.repository.RefreshTokenRepository;
import com.harmony.security.ReissueDto;
import com.harmony.security.TokenDto;
import com.harmony.security.utils.TokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
  private final TokenProvider tokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final RefreshTokenRepository refreshTokenRepository;

  public TokenDto login(LoginDto loginDto){
    log.info("login 메소드 진입");
    UsernamePasswordAuthenticationToken authenticationToken=
        new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

    log.info("authenticationToken 생성");

    // authenticate()에 CustomUserDetailService의 loadUserByUsername 실행
    try{
      Authentication authentication = authenticationManagerBuilder.getObject()
          .authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.info("Auth Service 작업이 끝났습니다.");

      TokenDto tokenDto=tokenProvider.generateTokenDto(authentication);
      RefreshToken refreshToken=RefreshToken.builder()
          .userEmail(loginDto.getEmail())
          .refreshToken(tokenDto.getRefreshToken())
          .build();

      refreshTokenRepository.save(refreshToken);

      return tokenDto;
    }catch(BadCredentialsException e){ // PW 불일치 캐치
      throw new AuthFailedException(
          ErrorCode.AUTH_FAILED
      );
    }
  }

  // TODO: Refresh Token을 Redis에서 관리, Custom Exception은 일단 보류
  public TokenDto reissue(ReissueDto reissueDto){
    // Refresh Token 검증
    if(!tokenProvider.isValidToken(reissueDto.getRefreshToken())){
      throw new RuntimeException("토큰이 유효하지 않음");
    }

    // Access Token에서 ID 가져오기
    // TODO: 아니면 token 안에 user email을 넣어야 하는가?
    Authentication authentication=tokenProvider.getAuthentication(reissueDto.getAccessToken());

    // ID를 기반으로 Server의 Refresh Token 값 가져오기
    RefreshToken refreshToken=refreshTokenRepository.findByUserEmail(authentication.getName())
        .orElseThrow(()->new RuntimeException("로그아웃된 사용자입니다."));

    // Refresh Token 값이 Server와 일치하는지 검사
    if(!refreshToken.getRefreshToken().equals(reissueDto.getRefreshToken())){
      throw new RuntimeException("Refresh Token 정보가 일치하지 않음");
    }

    TokenDto tokenDto=null;
    // TODO: 재발급 시점은 언제?
    if(tokenProvider.refreshTokenPeriodCheck(refreshToken.getRefreshToken())){
      // Refresh Token 유효 기간이 3일 미만일 경우 Access, Refresh 모두 재발급
      tokenDto=tokenProvider.generateTokenDto(authentication);

      RefreshToken newRefreshToken=refreshToken.updateRefreshToken(tokenDto.getRefreshToken());
      refreshTokenRepository.save(newRefreshToken);
    }else{
      // Refresh Token 유효 기간이 3일 이상일 경우 Access만 재발급
      tokenDto=TokenDto.builder()
          .accessToken(tokenProvider.generateAccessTokenOnly(authentication))
          .build();
    }

    return tokenDto;
  }
}
