package com.harmony.security;

import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@Getter
@AllArgsConstructor
public class UserDetailsDto implements UserDetails {
  private String email;
  private String password;
  private boolean enabled;
  private Collection<? extends GrantedAuthority> authorities;

  // TODO: authorities에 뭐 넣지?
  public UserDetailsDto(String email, String password, boolean enabled, String role) {
    this.email = email;
    this.password = password;
    this.enabled = enabled;
    this.authorities = createAuthorities(role);
  }

  private Collection<GrantedAuthority> createAuthorities(String role){
    Collection<GrantedAuthority> authorities=new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(role));
    return authorities;
  }
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword(){
    return this.password;
  }

  @Override
  public String getUsername(){
    return this.email;
  }

  // 인증 사용자의 계정 유효기간 만료 여부 반환
  @Override
  public boolean isAccountNonExpired(){
    return true;
  }

  // 인증 사용자의 계정 잠금 여부 반환
  @Override
  public boolean isAccountNonLocked(){
    return true;
  }

  // 인증 사용자의 비밀번호 유효기간 만료 여부 반환
  @Override
  public boolean isCredentialsNonExpired(){
    return true;
  }

  // 인증 사용자의 활성화 여부 반환
  @Override
  public boolean isEnabled(){
    return this.enabled;
  }
}
