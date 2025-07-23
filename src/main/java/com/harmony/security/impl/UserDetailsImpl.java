package com.harmony.security.impl;

import com.harmony.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class UserDetailsImpl implements UserDetails {
  @Getter
  private Long userId;
  private String email;
  private String userIdentifier;
  @Getter
  private String profileImageName;
  private String password;
  private String nickname;
  private boolean withdraw;

  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(User user) {
    this.userId = user.getUserId();
    this.email = user.getEmail();
    this.userIdentifier = user.getUserIdentifier();
    this.profileImageName = user.getProfileImageName();
    this.password = user.getPassword();
    this.nickname = user.getNickname();
    this.withdraw = user.getWithdraw();
    this.authorities = createAuthorities(user.getRole().toString());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword(){
    return password;
  }

  @Override
  public String getUsername(){
    return email;
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
    log.info("탈퇴 상태는 :"+withdraw+"입니다.");
    return !withdraw;
  }

  private Collection<GrantedAuthority> createAuthorities(String role){
    GrantedAuthority grantedAuthority=new SimpleGrantedAuthority(role);

    return Collections.singleton(grantedAuthority);
  }

  public String toString(){
    return this.userId+"번 사용자, "+this.nickname+", "+this.userIdentifier+", pfp:"+this.profileImageName;
  }
}
