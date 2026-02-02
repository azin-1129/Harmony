package com.harmony.service;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.dto.request.SearchUserRequestDto;
import com.harmony.dto.response.SearchUserResponseDto;
import com.harmony.dto.response.SelectProfileResponseDto;
import com.harmony.dto.response.SelectUserInfoResponseDto;
import com.harmony.entity.User;
import com.harmony.exception.DuplicatedUserNicknameException;
import com.harmony.exception.DuplicatedUserPasswordException;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.exception.InvalidArgumentException;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  private final AwsS3Service awsS3Service;

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
          .profileImageName(null) // TODO: 기본 프사 넣어줘야 할 듯?
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

  public List<SearchUserResponseDto> searchUser(SearchUserRequestDto searchUserRequestDto){
    String nicknameOrUserIdentifier= searchUserRequestDto.getNicknameOrUserIdentifier();
    List<User> searchUserResult=userRepository.findByNicknameContainsOrUserIdentifierContains(nicknameOrUserIdentifier, nicknameOrUserIdentifier);
    List<SearchUserResponseDto> searchUserResponseDtos=new ArrayList<>();
    for(User user:searchUserResult){
      String authorProfileImageName=user.getProfileImageName();
      SearchUserResponseDto searchUserResponseDto=SearchUserResponseDto.builder()
          .userId(user.getUserId())
          .profileImageUrl(authorProfileImageName==null? null:awsS3Service.getUserProfileImageUrl(user.getProfileImageName()))
          .nickname(user.getNickname())
          .userIdentifier(user.getUserIdentifier())
          .build();
      searchUserResponseDtos.add(searchUserResponseDto);
    }

    return searchUserResponseDtos;
  }

  public SelectUserInfoResponseDto findUserInfo(Long userId){
    User user=userRepository.findByUserId(userId).orElseThrow(
        ()->new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );

    SelectUserInfoResponseDto selectUserInfoResponseDto=
        SelectUserInfoResponseDto.builder()
            .userId(user.getUserId())
            .email(user.getEmail())
            .userIdentifier(user.getUserIdentifier())
            .profileImageUrl(awsS3Service.getUserProfileImageUrl(user.getProfileImageName()))
            .nickname(user.getNickname())
            .build();

    return selectUserInfoResponseDto;
  }

  public SelectProfileResponseDto selectProfile(Long userId){
    User user=userRepository.findByUserId(userId).orElseThrow(
        ()->new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );

    SelectProfileResponseDto selectProfileResponseDto=SelectProfileResponseDto.builder()
        .profileImageUrl(awsS3Service.getUserProfileImageUrl(user.getProfileImageName()))
        .nickname(user.getNickname())
        .userIdentifier(user.getUserIdentifier())
        .build();

    return selectProfileResponseDto;
  }

  public User getUserById(Long userId){
    return userRepository.findById(userId).orElseThrow(
        () -> new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );
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
    user.updatePassword(passwordEncoder.encode(newPassword));
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

  // 테스트용 삭제 메서드(로그인하지 않은 상태에서 실행)
  public void deleteUserForce(Long userId){
    User userToWithdraw=userRepository.findById(userId).orElseThrow(
        () -> new EntityNotFoundException(
            ErrorCode.USER_NOT_FOUND
        )
    );

    if(userToWithdraw.getWithdraw()){
      throw new UserAlreadyWithdrawException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    userToWithdraw.updateWithDraw(true);
  }
}
