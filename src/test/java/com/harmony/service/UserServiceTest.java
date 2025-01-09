package com.harmony.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.exception.EntityAlreadyExistException;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeAll
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

    userRepository.save(user);
  }

  @DisplayName("회원가입 테스트")
  @Order(1)
  @Test
  public void registerUser() {
    // given
    Long exectionId=2L;

    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
            .email("azin@kakao.com")
            .userIdentifier("kitii")
            .nickname("키티지니")
            .password("azin1129!")
            .passwordConfirm("azin1129!")
            .role(Role.MEMBER)
            .build();

    // when
    userService.registerUser(registerRequestDto);

    // then
    User savedUser=userRepository.findById(exectionId).get();
    log.info("두번째로 회원가입한 유저:"+savedUser.getUserId());
    assertEquals(exectionId, savedUser.getUserId());
  }

  @DisplayName("비밀번호 변경 테스트")
  @Order(2)
  @Test
  public void updateUserPassword(){
    // given
    Long userId=1L;

    // when
    String newPw="1130";
    userService.updateUserPassword(userId, newPw);
    User updatedUser=userRepository.findById(userId).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newPw, updatedUser.getPassword());
  }

  @DisplayName("닉네임 변경 테스트")
  @Order(3)
  @Test
  public void updateUserNickname(){
    // given
    Long userId=1L;

    // when
    String newNickname="치즈고양이";
    userService.updateUserNickname(userId, newNickname);
    User updatedUser=userRepository.findById(userId).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newNickname, updatedUser.getNickname());
  }

  @DisplayName("프로필 사진 변경 테스트")
  @Order(4)
  @Test
  public void updateUserProfileImage(){
    // given
    Long userId=1L;

    // when
    String newProfileImageName="black_kitten.png";
    userService.updateUserProfileImage(user.getUserId(), newProfileImageName);
    User updatedUser=userRepository.findById(user.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newProfileImageName, updatedUser.getProfileImageName());
  }

  @DisplayName("회원탈퇴 테스트")
  @Order(5)
  @Test
  public void deleteUser(){
    // given
    Long userId=2L;

    // when
    userService.deleteUser(userId);
    log.info("두번째 회원을 탈퇴했습니다.");

    User withDrawnUser=userRepository.findById(userId).orElse(null);

    // then
    assertEquals(true, withDrawnUser.getWithdraw());
  }

  // 예외 테스트

  // 이미 가입한 회원인 경우
  @DisplayName("회원가입 실패-이미 가입한 회원-닉네임 중복 테스트")
  @Order(6)
  @Test
  void registerUserFailedByDuplicatedNickname(){
    // given
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
  @Order(7)
  @Test
  void registerUserFailedByDuplicatedIdentifier(){
    // given
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
  @Order(8)
  @Test
  void registerUserFailedByDuplicatedEmail(){
    // given
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
  @DisplayName("회원탈퇴 실패-이미 탈퇴한 회원 테스트")
  @Order(9)
  @Test
  void registerUserFailedByAlreadyDeleted(){
    // given
    Long userId=2L;

    // when

    // then
    assertThrows(UserAlreadyWithdrawException.class, ()
        -> userService.deleteUser(userId));
  }

  @DisplayName("회원탈퇴 실패-존재하지 않는 회원 테스트")
  @Order(10)
  @Test
  void registerUserFailedByAlreadyWithdraw(){
    // given
    Long userId=3L;

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
        -> userService.deleteUser(userId));
  }
}