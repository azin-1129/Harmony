package com.harmony.security.impl;

import com.harmony.entity.User;
import com.harmony.exception.AuthFailedException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.exception.InvalidArgumentException;
import com.harmony.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  // 사용자 Email로 해당 사용자의 인증 정보 load
  @Override
  public UserDetails loadUserByUsername(String userEmail){
    log.info("userDetails service 진입");
    log.info("userEmail: {}", userEmail);

    return userRepository.findByEmail(userEmail)
        .map(user->createUserDetails(user))
        .orElseThrow(()->new AuthFailedException(ErrorCode.AUTH_FAILED));
  }

  private UserDetails createUserDetails(User user){
    GrantedAuthority grantedAuthority=new SimpleGrantedAuthority(user.getRole().toString());

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        Collections.singleton(grantedAuthority)
    );
  }
}
