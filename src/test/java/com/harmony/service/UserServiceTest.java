package com.harmony.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        .password("1129")
        .profileImageName("orange_orange_cat.png")
        .nickname("초코고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();
  }
  @AfterEach
  void clean(){
    userRepository.deleteAll();
  }

  @DisplayName("회원가입 테스트")
  @Test
  public void registerUser() {
    // given
    RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    registerRequestDto.setEmail("azin@naver.com");
    registerRequestDto.setUserIdentifier("choco");
    registerRequestDto.setPassword("1129");
    registerRequestDto.setRole(Role.MEMBER);

    // when
    RegisterResponseDto registerResponseDto = userService.registerUser(registerRequestDto);

    // then
    verify(userRepository, times(1)).save(any(User.class));
  }

  @DisplayName("로그인 테스트")
  @Test
  public void loginUser(){
    // given

    // when

    // then
  }

  @DisplayName("비밀번호 변경 테스트")
  @Test
  public void updateUserPassword(){
    // given
    User updateUser=userService.registerUser(user);

    // when
    String newPw="1130";
    userService.updatePassword(updateUser.getUserId(), newPw);
    User updatedUser=userRepository.findById(updateUser.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals("1130", updatedUser.getPassword());
  }

  @DisplayName("닉네임 변경 테스트")
  @Test
  public void updateUserNickname(){
    // given

    User updateUser=userService.registerUser(user);

    // when
    String newNickname="치즈고양이";
    userService.updateNickname(updateUser.getUserId(), newNickname);
    User updatedUser=userRepository.findById(updateUser.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals("치즈고양이", updatedUser.getNickname());
  }

  @DisplayName("프로필 사진 변경 테스트")
  @Test
  public void updateUserProfileImageName(){
    // given
    User updateUser=userService.registerUser(user);

    // when
    String newProfileImageName="black_kitten.png";
    userService.updateProfileImageName(updateUser.getUserId(), newProfileImageName);
    User updatedUser=userRepository.findById(updateUser.getUserId()).orElse(null);

    // then
    assertThat(updatedUser).isNotNull();
    assertEquals(newProfileImageName, updatedUser.getProfileImageName());
  }

  @DisplayName("로그아웃 테스트")
  @Test
  public void logoutUser(){
    // given

    // when

    // then
  }

  @DisplayName("회원탈퇴 테스트")
  @Test
  public void deleteUser(){
    // given
    Long userId=1L;

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository, times(1)).deleteById(userId);
  }
}