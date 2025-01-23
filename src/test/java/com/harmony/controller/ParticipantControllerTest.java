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
import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomForceRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.SelectChatRoomParticipantsRequestDto;
import com.harmony.dto.response.SelectChatRoomParticipantsResponseDto;
import com.harmony.dto.response.SelectParticipatedChatRoomsResponseDto;
import com.harmony.entity.ChatRoomType;
import com.harmony.service.ParticipantService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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

/**
 로그인한 user의 id는 1(choco)
 DM할 상대방 id는 2(cheese)

 choco-cheese 개인 채팅방 chatRoomId는 1L
 생성된 그룹 채팅방 chatRoomId는 2L
 **/

@WebMvcTest(controllers = ParticipantController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParticipantControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ParticipantService participantService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // CREATE

  // 개인 채팅방 참가+생성
  @DisplayName("개인 채팅방 참가+생성 테스트")
  @Order(1)
  @Test
  void createAndParticipatePersonalChatRoom() throws Exception {
    // given
    Long userId=1L;
    String partnerIdentifier="cheese";

    CreateAndParticipatePersonalChatRoomRequestDto createAndParticipatePersonalChatRoomRequestDto= CreateAndParticipatePersonalChatRoomRequestDto.builder()
        .partnerIdentifier(partnerIdentifier)
        .build();

    // stub
    when(participantService.createAndParticipatePersonalChatRoom(any(Long.class), any(CreateAndParticipatePersonalChatRoomRequestDto.class)))
        .thenReturn(1L);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/participate/init/personal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createAndParticipatePersonalChatRoomRequestDto)));

    // then
    resultActions
        .andExpect(status().isCreated())
        .andDo(print());

    verify(participantService, times(1)).createAndParticipatePersonalChatRoom(any(Long.class), any(CreateAndParticipatePersonalChatRoomRequestDto.class));
  }

  // 단체 채팅방 참가+생성
  @DisplayName("단체 채팅방 참가+생성 테스트")
  @Order(2)
  @Test
  void createAndParticipateGroupChatRoom() throws Exception {
    // given
    Long userId=1L;
    String chatRoomName="넙적당면에 죽고 못 사는 사람만";
    Integer chatRoomCountMax=3;

    CreateAndParticipateGroupChatRoomRequestDto createAndParticipateGroupChatRoomRequestDto= CreateAndParticipateGroupChatRoomRequestDto.builder()
        .chatRoomName(chatRoomName)
        .chatRoomCountMax(chatRoomCountMax)
        .build();

    // stub
    when(participantService.createAndParticipateGroupChatRoom(any(Long.class), any(CreateAndParticipateGroupChatRoomRequestDto.class)))
        .thenReturn(2L);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/participate/init/group")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createAndParticipateGroupChatRoomRequestDto)));

    // then
    resultActions
        .andExpect(status().isCreated())
        .andDo(print());

    verify(participantService, times(1)).createAndParticipateGroupChatRoom(any(Long.class), any(CreateAndParticipateGroupChatRoomRequestDto.class));
  }

  // 채팅방 참여
  @DisplayName("단체 채팅방 참여 테스트")
  @Order(3)
  @Test
  void participateGroupChatRoom() throws Exception {
    // given
    Long userId=1L;
    Long chatRoomId=2L;

    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto= ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(chatRoomId)
        .build();

    // stub
    when(participantService.participateGroupChatRoom(any(Long.class), any(ParticipateGroupChatRoomRequestDto.class)))
        .thenReturn(2L);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/participate/group")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(participateGroupChatRoomRequestDto)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(participantService, times(1)).participateGroupChatRoom(any(Long.class), any(ParticipateGroupChatRoomRequestDto.class));
  }

  // 채팅방 참가자 목록 조회
  @DisplayName("채팅방 참가자 목록 조회 테스트")
  @Order(4)
  @Test
  void selectChatRoomParticipants() throws Exception {
    // given
    Long chatRoomId=2L;
    SelectChatRoomParticipantsRequestDto selectChatRoomParticipantsRequestDto=SelectChatRoomParticipantsRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    List<SelectChatRoomParticipantsResponseDto> filteredParticipants=new ArrayList<>();

    SelectChatRoomParticipantsResponseDto userInfo= SelectChatRoomParticipantsResponseDto.builder()
        .userIdentifier("choco")
        .profileImageName("orange_orange_cat.png")
        .nickname("초코고양이")
        .build();

    filteredParticipants.add(userInfo);


    // stub
    when(participantService.selectChatRoomParticipants(any(SelectChatRoomParticipantsRequestDto.class)))
        .thenReturn(filteredParticipants);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
        .post("/participate/participants")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(selectChatRoomParticipantsRequestDto)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(participantService, times(1)).selectChatRoomParticipants(any(SelectChatRoomParticipantsRequestDto.class));
  }

  // 내가 참여한 채팅방 목록 조회
  @DisplayName("내가 참여한 채팅방 목록 조회 테스트")
  @Order(5)
  @Test
  void selectParticipatedChatRoom() throws Exception {
    // given
    Long userId=1L;

    List<SelectParticipatedChatRoomsResponseDto> filteredChatRooms=new ArrayList<>();

    SelectParticipatedChatRoomsResponseDto chatRoom1Info=SelectParticipatedChatRoomsResponseDto.builder()
        .chatRoomId(1L)
        .chatRoomName("")
        .chatRoomCount(2)
        .chatRoomCountMax(2)
        .chatRoomType(ChatRoomType.PERSONAL_CHATROOM)
        .build();

    filteredChatRooms.add(chatRoom1Info);

    SelectParticipatedChatRoomsResponseDto chatRoom2Info=SelectParticipatedChatRoomsResponseDto.builder()
        .chatRoomId(2L)
        .chatRoomName("넙적당면에 죽고 못 사는 사람만")
        .chatRoomCount(1)
        .chatRoomCountMax(3)
        .chatRoomType(ChatRoomType.GROUP_CHATROOM)
        .build();

    filteredChatRooms.add(chatRoom2Info);


    // stub
    when(participantService.selectParticipatedChatRooms(any(Long.class)))
        .thenReturn(filteredChatRooms);

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .get("/participate/participated"));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(participantService, times(1)).selectParticipatedChatRooms(any(Long.class));
  }

  // TODO: 채팅방 블랙리스트?
  // DELETE
  // 스스로 채팅방 퇴장
  @DisplayName("스스로 채팅방 퇴장 테스트")
  @Order(6)
  @Test
  void leaveChatRoom() throws Exception {
    // given
    Long userId=1L;
    Long chatRoomId=2L;

    LeaveChatRoomRequestDto leaveChatRoomRequestDto= LeaveChatRoomRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    // stub
    doNothing().when(participantService).leaveChatRoom(any(Long.class), any(LeaveChatRoomRequestDto.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/participate/leave")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(leaveChatRoomRequestDto)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(participantService, times(1)).leaveChatRoom(any(Long.class), any(LeaveChatRoomRequestDto.class));
  }

  // 채팅방 강제 퇴장
  @DisplayName("강제로 채팅방 퇴장 테스트")
  @Order(7)
  @Test
  void leaveChatRoomForce() throws Exception {
    // given
    String userIdentifier="cheese";
    Long chatRoomId=2L;

    LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto=LeaveChatRoomForceRequestDto.builder()
        .chatRoomId(chatRoomId)
        .userIdentifier(userIdentifier)
        .build();

    // stub
    doNothing().when(participantService).leaveChatRoomForce(any(LeaveChatRoomForceRequestDto.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/participate/leave-forced")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(leaveChatRoomForceRequestDto)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(participantService, times(1)).leaveChatRoomForce(any(LeaveChatRoomForceRequestDto.class));
  }

  // 예외

  // 단체 채팅방 생성 양식 오류(Validation)-validation 개발 필요
  @DisplayName("단체 채팅방 참가+생성 실패-기재 오류 테스트")
  @Order(8)
  @Test
  void createAndParticipateGroupChatRooFailedByInvalidArgument() throws Exception {
    // given
    Long userId=1L;
    String chatRoomName="빅보!"; // 오류사항
    Integer chatRoomCountMax=1; // 오류사항

    CreateAndParticipateGroupChatRoomRequestDto createAndParticipateGroupChatRoomRequestDto= CreateAndParticipateGroupChatRoomRequestDto.builder()
        .chatRoomName(chatRoomName)
        .chatRoomCountMax(chatRoomCountMax)
        .build();

    // stub

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/participate/init/group")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createAndParticipateGroupChatRoomRequestDto)));

    // then
    resultActions
        .andExpect(status().isBadRequest())
        .andDo(print());
  }
}