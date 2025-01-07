package com.harmony.service;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.User;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {
  private final UserRepository userRepository;

  // TODO: 예외처리, 닉네임 설정
  public void registerUser(RegisterRequestDto registerRequestDto) {
    User user= User.builder()
      .email(registerRequestDto.getEmail())
      .userIdentifier(registerRequestDto.getUserIdentifier())
      .password(registerRequestDto.getPassword())
      .profileImageName("")
      .nickname("닉변요망")
      .withdraw(false)
      .role(registerRequestDto.getRole())
      .build();

    userRepository.save(user);
  }

  // 비번 업뎃
  public void updateUserPassword(Long userId, String newPassword) {
    User user=userRepository.findById(userId).orElse(null);
    user.updatePassword(newPassword);
  }

  // 닉넴 업뎃
  public void updateUserNickname(Long userId, String newNickname) {
    User user=userRepository.findById(userId).orElse(null);
    user.updateNickname(newNickname);
  }

  // 프사 업뎃
  public void updateUserProfileImage(Long userId, String newProfileImageName) {
    User user=userRepository.findById(userId).orElse(null);
    user.updateProfileImage(newProfileImageName);
  }

  // 회원 탈퇴
  public void deleteUser(Long userId){
    userRepository.deleteById(userId);
  }
}
