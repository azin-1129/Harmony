package com.harmony.service;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.User;
import com.harmony.exception.DuplicatedUserNicknameException;
import com.harmony.exception.DuplicatedUserPasswordException;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.exception.InvalidArgumentException;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
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
    log.info("user Service 진입");
    HashMap<String, String> validateResult=new HashMap<>();

    if(!userRepository.findByEmail(registerRequestDto.getEmail()).isEmpty()) {
      validateResult.put("email", "이 이메일은 이미 등록되어 있습니다.");
    }

    if(!userRepository.findByUserIdentifier(registerRequestDto.getUserIdentifier()).isEmpty()){
      validateResult.put("userIdentifer", "이 식별자는 이미 등록되어 있습니다.");
    }
    if(!userRepository.findByNickname(registerRequestDto.getNickname()).isEmpty()){
      validateResult.put("nickname", "이 닉네임은 이미 등록되어 있습니다.");
    }

    log.info("validateResult={}", validateResult);

    if(validateResult.isEmpty()) {
      User user = User.builder()
          .email(registerRequestDto.getEmail())
          .userIdentifier(registerRequestDto.getUserIdentifier())
          .password(registerRequestDto.getPassword())
          .profileImageName("")
          .nickname(registerRequestDto.getNickname())
          .withdraw(false)
          .role(registerRequestDto.getRole())
          .build();

      userRepository.save(user);
    }else{
      log.info("회원가입 중복 오류를 반환해야 합니다.");
      throw new InvalidArgumentException(
          ErrorCode.USER_ALREADY_REGISTERED,
          validateResult
      );
    }
  }

  // 비번 업뎃
  public void updateUserPassword(Long userId, String newPassword) {
    User user=userRepository.findById(userId).orElse(null);
    if(user.getPassword().equals(newPassword)){
      throw new DuplicatedUserPasswordException(
          ErrorCode.DUPLICATED_USER_PASSWORD
      );
    }
    user.updatePassword(newPassword);
  }

  // 닉넴 업뎃
  public void updateUserNickname(Long userId, String newNickname) {
    User user=userRepository.findById(userId).orElse(null);
    if(user.getNickname().equals(newNickname)){
      throw new DuplicatedUserNicknameException(
          ErrorCode.DUPLICATED_USER_NICKNAME
      );
    }
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
