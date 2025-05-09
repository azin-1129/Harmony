package com.harmony.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harmony.dto.form.NicknameForm;
import com.harmony.dto.form.PasswordForm;
import com.harmony.dto.form.ProfileImageForm;
import com.harmony.dto.request.RegisterRequestDto;
import com.harmony.entity.Role;
import com.harmony.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
    objectMapper=new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // 회원 가입
  @DisplayName("회원가입 테스트")
  @Test
  void registerUser() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .nickname("초코고양이")
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.ROLE_MEMBER)
        .build();

    // stub
    doNothing().when(userService).registerUser(any(RegisterRequestDto.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isCreated())
        .andDo(print());

    verify(userService, times(1)).registerUser(any(RegisterRequestDto.class));
  }

  // 비밀번호 변경
  @DisplayName("비밀번호 변경 테스트")
  @Test
  void updateUserPassword() throws Exception {
    // given
    Long updateUserId=1L; // @CurrentUser로 빼올 예정
    PasswordForm passwordForm=PasswordForm.builder() // @Valid 사용 예정
      .newPassword("azin1129?")
      .newPasswordConfirm("azin1129?")
      .build();

    String newPw=passwordForm.getNewPassword();

    // stub
    doNothing().when(userService).updateUserPassword(updateUserId, newPw);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/update/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordForm)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(userService, times(1)).updateUserPassword(updateUserId, newPw);
  }

  // 닉네임 변경
  @DisplayName("닉네임 변경 테스트")
  @Test
  void updateUserNickname() throws Exception {
    // given
    Long updateUserId=1L; // @CurrentUser로 빼올 예정
    NicknameForm nicknameForm=NicknameForm.builder()
      .newNickname("치즈고양이")
      .build();

    String newNickname=nicknameForm.getNewNickname();

    // stub
    doNothing().when(userService).updateUserNickname(updateUserId, newNickname);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/update/nickname")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nicknameForm)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(userService, times(1)).updateUserNickname(updateUserId, newNickname);
  }

  // 프로필 사진 변경
  @DisplayName("프로필 사진 변경 테스트")
  @Test
  void updateUserProfileImageName() throws Exception {
    // given
    Long updateUserId=1L; // @CurrentUser로 빼올 예정
    ProfileImageForm profileImageForm=ProfileImageForm.builder()
      .newProfileImageName("black_kitten.png")
      .build();

    String newProfileImageName=profileImageForm.getNewProfileImageName();

    // stub
    doNothing().when(userService).updateUserProfileImage(updateUserId, newProfileImageName);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/update/profile-image")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileImageForm)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(userService, times(1)).updateUserProfileImage(updateUserId, newProfileImageName);
  }

  // 회원 탈퇴
  @DisplayName("회원탈퇴 테스트")
  @Test
  void deleteUser() throws Exception {
    // given
    Long userId=1L;

    // stub
    doNothing().when(userService).deleteUser(any(Long.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/user/"));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(userService, times(1)).deleteUser(any(Long.class));
  }

  // 예외 테스트

  // 빈 칸인 경우
  @DisplayName("회원가입 실패-기재오류-빈칸 테스트")
  @Test
  void registerUserFailedByBlank() throws Exception {
    // given
    RegisterRequestDto registerRequestDto= RegisterRequestDto.builder()
        .build();

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  // 기재 형식이 잘못된 경우
  @DisplayName("회원가입 실패-기재오류-비밀번호 테스트")
  @Test
  void registerUserFailedByInvalidPasswordArgument() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .nickname("초코고양이")
        .password("1129") // 오류사항
        .role(Role.ROLE_MEMBER)
        .build();

    // when
    ResultActions resultActions =
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @DisplayName("회원가입 실패-기재오류-비밀번호 불일치 테스트")
  @Test
  void registerUserFailedByFailedPasswsordConfirm() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .nickname("초코고양이")
        .password("azin1129!")
        .passwordConfirm("azin112!") // 오류사항
        .role(Role.ROLE_MEMBER)
        .build();

    // when
    ResultActions resultActions =
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @DisplayName("회원가입 실패-기재오류-비밀번호 확인 미입력 테스트")
  @Test
  void registerUserFailedByBlankPasswsordConfirm() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .nickname("초코고양이")
        .password("azin1129!")
        .passwordConfirm("") // 오류사항
        .role(Role.ROLE_MEMBER)
        .build();

    // when
    ResultActions resultActions =
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @DisplayName("회원가입 실패-기재오류-식별자 테스트")
  @Test
  void registerUserFailedByInvalidIdentifierArgument() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("CHOCO") // 오류사항
        .nickname("초코고양이")
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.ROLE_MEMBER)
        .build();

    // when
    ResultActions resultActions =
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @DisplayName("회원가입 실패-기재오류-닉네임 테스트")
  @Test
  void registerUserFailedByInvalidNicknameArgument() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .nickname("초") // 오류사항
        .password("azin1129!")
        .passwordConfirm("azin1129!")
        .role(Role.ROLE_MEMBER)
        .build();

    // when
    ResultActions resultActions =
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }
}