package com.harmony.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.harmony.dto.request.RegisterRequestDto;
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
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
            .email("azin@naver.com")
            .userIdentifier("choco")
            .password("1129")
            .role(Role.MEMBER)
            .build();

    // when
    userService.registerUser(registerRequestDto);

    // then
    verify(userRepository, times(1)).save(any(User.class));
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
    assertEquals("1130", updatedUser.getPassword());
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
    assertEquals("치즈고양이", updatedUser.getNickname());
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

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository, times(1)).deleteById(userId);
  }
}