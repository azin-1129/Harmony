package com.harmony.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.service.ChatRoomService;
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

@WebMvcTest(controllers = ChatRoomController.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatRoomControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChatRoomService chatRoomService;

  private ObjectMapper objectMapper;

  @BeforeAll
  void setUp(){
    objectMapper=new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡCREATEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡREADㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // TODO: 참여한 채팅방 정보 조회

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡUPDATEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // 채팅방 이름 변경
  @DisplayName("채팅방 이름 변경 테스트")
  @Test
  void updateChatRoomName() throws Exception {
    // given
    Long chatRoomId=1L;
    String newChatRoomName="배고픈 사람 들어와봐";
    ChatRoomNameForm chatRoomNameForm=ChatRoomNameForm.builder()
        .chatRoomId(chatRoomId)
        .newChatRoomName(newChatRoomName)
        .build();

    // stub
    doNothing().when(chatRoomService).updateChatRoomName(any(ChatRoomNameForm.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .post("/chatroom/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(chatRoomNameForm)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(chatRoomService, times(1)).updateChatRoomName(any(ChatRoomNameForm.class));
  }

  // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡDELETEㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

  // 채팅방 삭제
  @DisplayName("채팅방 삭제 테스트")
  @Test
  void deleteChatRoom() throws Exception {
    // given
    Long chatRoomId=1L;

    // stub
    doNothing().when(chatRoomService).deleteChatRoom(any(Long.class));

    // when
    ResultActions resultActions=
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/chatroom/"+chatRoomId));

    // then
    resultActions
        .andExpect(status().isOk())
        .andDo(print());

    verify(chatRoomService, times(1)).deleteChatRoom(any(Long.class));
  }

  // 예외테스트

}