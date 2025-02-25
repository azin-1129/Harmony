package com.harmony.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harmony.dto.response.FriendshipResponseDto;
import com.harmony.entity.FriendType;
import com.harmony.service.FriendshipService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = FriendController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FriendshipService friendshipService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
    objectMapper=new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡCREATEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡREADㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // 친구 검색
  @DisplayName("친구 검색 테스트")
  @Test
  void readFriendships() throws Exception{
    // given
    List<FriendshipResponseDto> friendshipResponseDtos =new ArrayList<>();
    FriendshipResponseDto friendshipResponseDto= FriendshipResponseDto.builder()
        .friendIdentifier("pikmin")
        .nickname("보라뚱돼지")
        .profileImageName("보뚱이.jpg")
        .friendType(FriendType.FRIEND)
        .build();
    friendshipResponseDtos.add(friendshipResponseDto);

    // stub
    when(friendshipService.readFriendships(any(Long.class)))
        .thenReturn(friendshipResponseDtos);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .get("/friendship/friendships"));

    // then
    resultActions
        .andExpect(status().isOk())
            .andExpect(jsonPath("$..data[0].friendIdentifier").value("pikmin"))
            .andExpect(jsonPath("$..data[0].nickname").value("보라뚱돼지"))
            .andExpect(jsonPath("$..data[0].profileImageName").value("보뚱이.jpg"))
            .andExpect(jsonPath("$..data[0].friendType").value("FRIEND"));

    verify(friendshipService, times(1)).readFriendships(any(Long.class));

  }
  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡUPDATEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡDELETEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

}