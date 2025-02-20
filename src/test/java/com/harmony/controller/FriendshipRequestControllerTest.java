package com.harmony.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harmony.dto.response.ReceivedFriendshipRequestResponseDto;
import com.harmony.dto.response.SentFriendshipRequestResponseDto;
import com.harmony.service.FriendshipRequestService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers=FriendshipRequestController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendshipRequestControllerTest {
  // void 메서드 외 return 타입 테스트
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FriendshipRequestService friendshipRequestService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // 보낸 요청
  @DisplayName("보낸 친구요청 목록 테스트")
  @Test
  void sentFriendshipRequests() throws Exception {
    // given
    String senderIdentifier="choco";
    String receiverIdentifier="cheese";

    List<SentFriendshipRequestResponseDto> sentFriendshipRequests=new ArrayList<>();

    SentFriendshipRequestResponseDto sentFriendshipRequestResponseDto= SentFriendshipRequestResponseDto.builder()
        .friendshipRequestSenderIdentifier(senderIdentifier)
        .friendshipRequestReceiverIdentifier(receiverIdentifier)
        .build();

    sentFriendshipRequests.add(sentFriendshipRequestResponseDto);

    // stub
    when(friendshipRequestService.sentFriendshipRequest(any(Long.class)))
        .thenReturn(sentFriendshipRequests);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .get("/friendship-request/sent"));
    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(friendshipRequestService, times(1)).sentFriendshipRequest(any(Long.class));
  }
  // 받은 요청
  @DisplayName("받은 친구요청 목록 테스트")
  @Test
  void receivedFriendshipRequests() throws Exception {
    // given
    String senderIdentifier="oreo";
    String receiverIdentifier="choco";

    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequests=new ArrayList<>();

    ReceivedFriendshipRequestResponseDto receivedFriendshipRequestResponseDto= ReceivedFriendshipRequestResponseDto.builder()
        .friendshipRequestSenderIdentifier(senderIdentifier)
        .friendshipRequestReceiverIdentifier(receiverIdentifier)
        .build();

    receivedFriendshipRequests.add(receivedFriendshipRequestResponseDto);

    // stub
    when(friendshipRequestService.receivedFriendshipRequest(any(Long.class)))
        .thenReturn(receivedFriendshipRequests);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .get("/friendship-request/received"));
    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(friendshipRequestService, times(1)).receivedFriendshipRequest(any(Long.class));
  }
}