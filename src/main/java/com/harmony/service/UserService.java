package com.harmony.service;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.User;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityAlreadyExistException;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {
  private final UserRepository userRepository;

  // TODO: 이메일, 식별자, 닉네임 중복 처리 이렇게 해도 되나?
  public void registerUser(RegisterRequestDto registerRequestDto) {
    if(!userRepository.findByEmail(registerRequestDto.getEmail()).isEmpty()){
      throw new EntityAlreadyExistException(
          ErrorCode.USER_ALREADY_REGISTERED
      );
    } else if(!userRepository.findByUserIdentifier(registerRequestDto.getUserIdentifier()).isEmpty()){
      throw new EntityAlreadyExistException(
          ErrorCode.USER_ALREADY_REGISTERED
      );
    } else if(!userRepository.findByNickname(registerRequestDto.getNickname()).isEmpty()){
      throw new EntityAlreadyExistException(
          ErrorCode.USER_ALREADY_REGISTERED
      );
    }

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

  // TODO: withdraw 값 기준으로 벌크 연산
  // 회원 탈퇴
  public void deleteUser(Long userId){
    Optional<User> userToWithDraw=userRepository.findById(userId);
    log.info("userId 기반으로 조회한 user:"+userToWithDraw.toString());

    if(!userToWithDraw.isPresent()){
      throw new EntityNotFoundException(
          ErrorCode.USER_NOT_FOUND
      );
    }else if(userToWithDraw.get().getWithdraw()==true){
      throw new UserAlreadyWithdrawException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    userToWithDraw.get().updateWithDraw(true);
//    userRepository.deleteById(userToWithDraw.get().getUserId());
  }
}
