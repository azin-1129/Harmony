package com.harmony.service;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.GroupChatRoomCreateRequestDto;
import com.harmony.dto.request.PersonalChatRoomCreateRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;

  // TODO: 이럴 거면 그냥 서버 단에서 채팅방 타입 지정하면 되지 않나?
  // 개인챗방 생성
  public void createPersonalChatRoom(PersonalChatRoomCreateRequestDto personalChatRoomCreateRequestDto) {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName(personalChatRoomCreateRequestDto.getChatRoomName())
        .chatRoomCount(personalChatRoomCreateRequestDto.getChatRoomCount())
        .chatRoomCountMax(personalChatRoomCreateRequestDto.getChatRoomCountMax())
        .chatRoomType(personalChatRoomCreateRequestDto.getChatRoomType())
        .build();

    chatRoomRepository.save(chatRoom);
    log.info("생성중인 개인 채팅방 id:"+chatRoom.getChatRoomId());
  }

  // 단체챗방 생성
  public void createGroupChatRoom(GroupChatRoomCreateRequestDto groupChatRoomCreateRequestDto) {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName(groupChatRoomCreateRequestDto.getChatRoomName())
        .chatRoomCount(groupChatRoomCreateRequestDto.getChatRoomCount())
        .chatRoomCountMax(groupChatRoomCreateRequestDto.getChatRoomCountMax())
        .chatRoomType(groupChatRoomCreateRequestDto.getChatRoomType())
        .build();

    chatRoomRepository.save(chatRoom);
    log.info("생성중인 단체 채팅방 id:"+chatRoom.getChatRoomId());
  }

  // 채팅방 이름 변경
  public void updateChatRoomName(ChatRoomNameForm chatRoomNameForm){
    ChatRoom chatRoom=chatRoomRepository.findById(chatRoomNameForm.getChatRoomId()).get();

    chatRoom.updateChatRoomName(chatRoomNameForm.getNewChatRoomName());
  }

  // 채팅방 삭제
  public void deleteChatRoom(Long chatRoomId){
    ChatRoom chatRoom=chatRoomRepository.findById(chatRoomId).get();

    chatRoomRepository.delete(chatRoom);
  }
}
