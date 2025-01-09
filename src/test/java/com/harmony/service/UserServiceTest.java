package com.harmony.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.global.response.exception.EntityAlreadyExistException;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp(){
    user= User.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .password("azin1129!")
        .profileImageName("orange_orange_cat.png")
        .nickname("초코고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();
  }
  @AfterEach
  void clean(){
    log.info("🧽🧽🧽🧽clean()🧽🧽🧽🧽");
    userRepository.deleteAll();
  }

  @DisplayName("회원가입 테스트")
  @Test
  public void registerUser() {
    // given
    Long exectionId=1L;

    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
            .email("azin@naver.com")
            .userIdentifier("choco")
            .nickname("초코고양이")
            .password("azin1129!")
            .passwordConfirm("azin1129!")
            .role(Role.MEMBER)
            .build();

    // when
    userService.registerUser(registerRequestDto);

    // then
    User savedUser=userRepository.findById(exectionId).get();
    assertEquals(exectionId, savedUser.getUserId());
  }

  @DisplayName("비밀번호 변경 테스트")
  @Test
  public void updateUserPassword(){
    // given
    userRepository.save(user);

    // when
    String newPw="1130";
    userService.updateUserPassword(user.getUserId(), newPw);
    User updatedUser=userRepository.findById(user.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newPw, updatedUser.getPassword());
  }

  @DisplayName("닉네임 변경 테스트")
  @Test
  public void updateUserNickname(){
    // given
    userRepository.save(user);

    // when
    String newNickname="치즈고양이";
    userService.updateUserNickname(user.getUserId(), newNickname);
    User updatedUser=userRepository.findById(user.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newNickname, updatedUser.getNickname());
  }

  @DisplayName("프로필 사진 변경 테스트")
  @Test
  public void updateUserProfileImage(){
    // given
    userRepository.save(user);

    // when
    String newProfileImageName="black_kitten.png";
    userService.updateUserProfileImage(user.getUserId(), newProfileImageName);
    User updatedUser=userRepository.findById(user.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newProfileImageName, updatedUser.getProfileImageName());
  }

  @DisplayName("회원탈퇴 테스트")
  @Test
  public void deleteUser(){
    // given
    Long userId=1L;
    userRepository.save(user);

    // when

    // then
   assertThrows(EntityNotFoundException.class, () ->
       userService.deleteUser(userId));
  }

  // 예외 테스트

  // 이미 가입한 회원인 경우
  @DisplayName("회원가입 실패-이미 가입한 회원-닉네임 중복 테스트")
  @Test
  void registerUserFailedByDuplicatedNickname(){
    // given
    userRepository.save(user);

    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@kakao.com")
        .userIdentifier("cherry")
        .nickname("초코고양이") // 오류사항
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.MEMBER)
        .build();

    // when

    // then
    assertThrows(EntityAlreadyExistException.class, ()
    -> userService.registerUser(registerRequestDto));
  }

  @DisplayName("회원가입 실패-이미 가입한 회원-식별자 중복 테스트")
  @Test
  void registerUserFailedByDuplicatedIdentifier(){
    // given
    userRepository.save(user);

    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@kakao.com")
        .userIdentifier("choco") // 오류사항
        .nickname("치즈고양이")
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.MEMBER)
        .build();

    // when

    // then
    assertThrows(EntityAlreadyExistException.class, ()
        -> userService.registerUser(registerRequestDto));
  }

  @DisplayName("회원가입 실패-이미 가입한 회원-이메일 중복 테스트")
  @Test
  void registerUserFailedByDuplicatedEmail(){
    // given
    userRepository.save(user);

    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com") // 오류사항
        .userIdentifier("choco13")
        .nickname("치즈고양이")
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.MEMBER)
        .build();

    // when

    // then
    assertThrows(EntityAlreadyExistException.class, ()
        -> userService.registerUser(registerRequestDto));
  }

  // TODO: WithDraw로 판단해야 함
  // 이미 탈퇴한 회원인 경우
  @DisplayName("회원가입 실패-이미 탈퇴한 회원 테스트")
  @Test
  void registerUserFailedByAlreadyDeleted(){
    // given
    Long userId=1L;

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
        -> userService.deleteUser(userId));
  }
}