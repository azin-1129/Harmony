package com.harmony.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harmony.entity.Role;
import com.harmony.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
  }

  @BeforeEach
  public void init(){
    objectMapper=new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // 회원 가입
  @DisplayName("회원가입 테스트")
  @Test
  void registerUser() throws Exception {
    // given
    RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    registerRequestDto.setEmail("azin@naver.com");
    registerRequestDto.setUserIdentifier("choco");
    registerRequestDto.setPassword("1129");
    registerRequestDto.setRole(Role.MEMBER);

    RegisterResponseDto registerResponseDto=new RegisterResponseDto();

    // stub
    when(userService.registerUser(any(RegisterRequestDto.class)))
        .thenReturn(any(RegisterResponseDto.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/member")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDto)));

    // then
    resultActions
        .andExpect(status().isCreated())
        .andDo(print());

    verify(userService).registerUser(any(RegisterRequestDto.class));
  }

  // TODO: Update test 작성
  // 비밀번호 변경

  // 닉네임 변경

  // 프로필 사진 변경

  // 회원 탈퇴
  @DisplayName("회원탈퇴 테스트")
  @Test
  void deleteUser() throws Exception {
    // given
    Long userId=1L;

    // stub
    doNothing().when(userService).deleteUser(userId);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/member/"+userId));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(userService, times(1)).deleteUser(userId);
  }
}