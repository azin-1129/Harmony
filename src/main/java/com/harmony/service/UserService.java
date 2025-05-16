package com.harmony.service;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.dto.response.SelectUserInfoResponseDto;
import com.harmony.entity.User;
import com.harmony.exception.DuplicatedUserNicknameException;
import com.harmony.exception.DuplicatedUserPasswordException;
import com.harmony.exception.SecurityContextNotFoundException;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.exception.InvalidArgumentException;
import com.harmony.repository.UserRepository;
import com.harmony.security.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // TODO: 이메일, 식별자, 닉네임 중복 처리 이렇게 해도 되나?
  public void registerUser(RegisterRequestDto registerRequestDto) {
    log.info("user Service 진입");
    HashMap<String, String> validateResult=new HashMap<>();

    // TODO: 이거 내용 따로 뺄 수 없나?
    if(!userRepository.findByEmail(registerRequestDto.getEmail()).isEmpty()) {
      validateResult.put("email", "이 이메일은 이미 등록되어 있습니다.");
    }

    if(!userRepository.findByUserIdentifier(registerRequestDto.getUserIdentifier()).isEmpty()){
      validateResult.put("userIdentifer", "이 식별자는 이미 등록되어 있습니다.");
    }
    if(!userRepository.findByNickname(registerRequestDto.getNickname()).isEmpty()){
      validateResult.put("nickname", "이 닉네임은 이미 등록되어 있습니다.");
    }

    if(validateResult.isEmpty()) {
      User user = User.builder()
          .email(registerRequestDto.getEmail())
          .userIdentifier(registerRequestDto.getUserIdentifier())
          .password(passwordEncoder.encode(registerRequestDto.getPassword()))
          .profileImageName("") // TODO: 기본 프사 넣어줘야 할 듯?
          .nickname(registerRequestDto.getNickname())
          .withdraw(false)
          .role(registerRequestDto.getRole())
          .build();

      userRepository.save(user);
    }else{
      throw new InvalidArgumentException(
          ErrorCode.USER_ALREADY_REGISTERED,
          validateResult
      );
    }
  }

  public SelectUserInfoResponseDto findUserInfo(){
    String email= SecurityUtil.getCurrentEmail().orElseThrow(()->
        new SecurityContextNotFoundException(
            ErrorCode.AUTH_CONTEXT_NOT_FOUND
        )
    );

    User user=userRepository.findByEmail(email).orElseThrow(
        ()->new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );

    SelectUserInfoResponseDto selectUserInfoResponseDto=
        SelectUserInfoResponseDto.builder()
            .email(email)
            .userIdentifier(user.getUserIdentifier())
            .profileImageName(user.getProfileImageName())
            .nickname(user.getNickname())
            .build();

    return selectUserInfoResponseDto;
  }

  public User getUserById(Long userId){
    return userRepository.findById(userId).get();
  }

  public User getUserByUserIdentifier(String userIdentifier) {

    return userRepository.findByUserIdentifier(userIdentifier).orElseThrow(
        ()        -> new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );
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
  }

  // 테스트용 삭제 메서드
  public void deleteUserForce(Long userId){
    userRepository.deleteById(userId);
  }
}
