package com.harmony.security.utils;

import com.harmony.security.impl.UserDetailsImpl;
import com.harmony.security.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.security.SignatureException;
import com.harmony.exception.ExpiredTokenException;
import com.harmony.exception.InvalidTokenException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.security.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
@Component
public class TokenProvider {
  // 환경변수
  private static final String AUTHORITIES_KEY="auth";
  private static final String BEARER_TYPE="Bearer";
  private final long ACCESS_TOKEN_EXPIRE_TIME;
  private final long REFRESH_TOKEN_EXPIRE_TIME;
  private static final long THREE_DAYS=1000*60*60*24*3;

  private SecretKey key; // JWT Secret key
  private final UserDetailsServiceImpl userDetailsServiceImpl;

  public TokenProvider(@Value("${jwt.secret}") String jwtSecretKey,
      UserDetailsServiceImpl userDetailsServiceImpl,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration){
    this.userDetailsServiceImpl = userDetailsServiceImpl;
    byte[] keyBytes= Decoders.BASE64.decode(jwtSecretKey);
    this.key=Keys.hmacShaKeyFor(keyBytes);
    this.ACCESS_TOKEN_EXPIRE_TIME=accessTokenExpiration;
    this.REFRESH_TOKEN_EXPIRE_TIME=refreshTokenExpiration;
  }

  // TODO: Subject가 필요한가? 그것보다 JWT를 어떻게 구성할 것인가?
  // 사용자 정보를 기반으로 Token을 생성하여 반환
  public TokenDto generateTokenDto(Authentication authentication){
    log.debug("생성된 JWT Secret Key: "+key);
    String authorities=authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    String accessToken=generateAccessToken(authentication.getName(), authorities);
    String refreshToken=generateRefreshToken(authentication.getName(), authorities);

    return TokenDto.builder()
        .grantType(BEARER_TYPE)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public String generateAccessTokenOnly(Authentication authentication){
    String authorities=authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return generateAccessToken(authentication.getName(), authorities);
  }

  private String generateAccessToken(String email, String authorities){
    return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .claims(createClaims(authorities))
        .signWith(key)
        .expiration(createAccessTokenExpiredDate())
        .compact();
  }
  private String generateRefreshToken(String email, String authorities){
    log.debug("JWT Secret Key: "+key);

    return Jwts.builder()
        .subject(email)
        .claims(createClaims(authorities))
//        .claim("isRefreshToken", true)
        .signWith(key)
        .expiration(createRefreshTokenExpiredDate())
        .compact();
  }

  // AuthenticationFilter에서 활용
  public Authentication getAuthentication(String token){
    log.info("get Authentication에 진입합니다.");
    Claims claims=parseClaims(token);
    log.info("parse를 완료했습니다.");

    if(claims.get(AUTHORITIES_KEY)==null){
      log.error("claim 내 auth key가 null 입니다.");
      throw new InvalidTokenException(
          ErrorCode.AUTH_INVALID_TOKEN
      );
    }

    // Claim에서 권한 정보 추출
    Collection<? extends GrantedAuthority> authorities=
        Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    UserDetailsImpl principal=userDetailsServiceImpl.loadUserByUsername(claims.getSubject());

    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }
  // Refresh Token 유효기간 지정
  private Date createRefreshTokenExpiredDate(){
    long now=(new Date()).getTime();
    return new Date(now+REFRESH_TOKEN_EXPIRE_TIME);
  }

  // 토큰 만료기간 지정
  private Date createAccessTokenExpiredDate(){
    long now=(new Date()).getTime();
    return new Date(now+ACCESS_TOKEN_EXPIRE_TIME);
  }

  // 사용자 정보 기반으로 Claims 생성
  private static Map<String, Object> createClaims(String authorities){
    Map<String, Object> claims=new HashMap<>();

    log.info("auth:"+authorities);

    claims.put(AUTHORITIES_KEY, authorities);
    return claims;
  }

  // Token을 기반으로 유효 토큰 여부 반환. AuthenticationFilter에서 활용
  public boolean isValidToken(String token){
    try{
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (SignatureException | SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      throw new InvalidTokenException(
          ErrorCode.AUTH_INVALID_TOKEN
      );
    } catch (ExpiredJwtException e) {
      throw new ExpiredTokenException(
          ErrorCode.AUTH_EXPIRED_TOKEN
      );
    }
  }

  // JWT 내에서 Claims 정보 반환
  private Claims parseClaims(String accessToken) {
    try {
      log.info("parse 작업을 시작합니다.");
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload();
    }  catch (SignatureException | SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      throw new InvalidTokenException(
          ErrorCode.AUTH_INVALID_TOKEN
      );
    }catch (ExpiredJwtException e) {
      log.error("만료된 토큰입니다.");
      throw new ExpiredTokenException(
          ErrorCode.AUTH_EXPIRED_TOKEN
      );
    }
  }

  public boolean refreshTokenPeriodCheck(String refreshToken){
    Jws<Claims> claimsJws=Jwts.parser().setSigningKey(key).build().parseClaimsJws(refreshToken);
    long now=(new Date()).getTime();
    long refreshExpiredTime=claimsJws.getBody().getExpiration().getTime();
    long refreshNowTime=new Date(now+REFRESH_TOKEN_EXPIRE_TIME).getTime();

    if(refreshNowTime-refreshExpiredTime>THREE_DAYS){
      return true;
    }
    return false;
  }

  public String getUsername(String accessToken){
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload().getSubject();
  }
}
