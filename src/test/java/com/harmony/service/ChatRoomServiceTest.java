package com.harmony.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.GroupChatRoomCreateRequestDto;
import com.harmony.dto.request.PersonalChatRoomCreateRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.ChatRoomType;
import com.harmony.repository.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatRoomServiceTest {
  @Autowired
  private ChatRoomService chatRoomService;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  // 개인챗 생성
  @DisplayName("개인 채팅방 생성 테스트")
  @Order(1)
  @Test
  public void createPersonalChatRoom(){
    // given
    Long expectionChatRoomId=1L;

    PersonalChatRoomCreateRequestDto personalChatRoomCreateRequestDto=PersonalChatRoomCreateRequestDto.builder()
        .chatRoomName("개인 채팅방")
        .chatRoomCount(0)
        .chatRoomCountMax(2)
        .chatRoomType(ChatRoomType.PERSONAL_CHATROOM)
        .build();

    // when
    chatRoomService.createPersonalChatRoom(personalChatRoomCreateRequestDto);

    // then
    ChatRoom chatRoom=chatRoomRepository.findById(expectionChatRoomId).get();
    assertEquals(expectionChatRoomId,chatRoom.getChatRoomId());
  }
  // 단체챗 생성
  @DisplayName("단체 채팅방 생성 테스트")
  @Order(2)
  @Test
  public void createGroupChatRoom(){
    // given
    Long expectionChatRoomId=2L;
    GroupChatRoomCreateRequestDto groupChatRoomCreateRequestDto=GroupChatRoomCreateRequestDto.builder()
        .chatRoomName("심심한 사람만")
        .chatRoomCount(0)
        .chatRoomCountMax(10)
        .chatRoomType(ChatRoomType.GROUP_CHATROOM)
        .build();

    // when
    chatRoomService.createGroupChatRoom(groupChatRoomCreateRequestDto);

    // then
    ChatRoom chatRoom=chatRoomRepository.findById(expectionChatRoomId).get();
    assertEquals(expectionChatRoomId,chatRoom.getChatRoomId());
  }

  // 채팅방 이름 변경
  @DisplayName("채팅방 이름 변경 테스트")
  @Order(3)
  @Test
  public void updateChatRoomName(){
    // given
    Long chatRoomId=2L;
    String newChatRoomName="마라탕 좋아하는 사람 모임";
    ChatRoomNameForm chatRoomNameForm=ChatRoomNameForm.builder()
        .chatRoomId(chatRoomId)
        .newChatRoomName(newChatRoomName)
        .build();

    // when
    chatRoomService.updateChatRoomName(chatRoomNameForm);
    ChatRoom updatedChatRoom=chatRoomRepository.findById(chatRoomId).get();

    // then
    assertThat(updatedChatRoom).isNotNull();
    assertEquals(newChatRoomName,updatedChatRoom.getChatRoomName());
  }

  // 채팅방 삭제
  @DisplayName("채팅방 삭제 테스트")
  @Order(4)
  @Test
  public void deleteChatRoom(){
    // given
    Long chatRoomId=2L;

    // when
    chatRoomService.deleteChatRoom(chatRoomId);
    ChatRoom deletedChatRoom=chatRoomRepository.findById(chatRoomId).orElse(null);

    // then
    assertNull(deletedChatRoom);
  }
}