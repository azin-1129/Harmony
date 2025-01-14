package com.harmony.service;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.GroupChatRoomCreateRequestDto;
import com.harmony.dto.request.PersonalChatRoomCreateRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.ChatRoomType;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;

  // TODO: 하드 코딩을 환경변수로 따로 떼기
  // TODO: 참가 처리
  // 개인챗방 생성
  public void createPersonalChatRoom(PersonalChatRoomCreateRequestDto personalChatRoomCreateRequestDto) {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName("")
        .chatRoomCount(2)
        .chatRoomCountMax(2)
        .chatRoomType(ChatRoomType.PERSONAL_CHATROOM)
        .build();

    chatRoomRepository.save(chatRoom);
    log.info("생성중인 개인 채팅방 id:"+chatRoom.getChatRoomId());
  }

  // 단체챗방 생성
  public void createGroupChatRoom(GroupChatRoomCreateRequestDto groupChatRoomCreateRequestDto) {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName(groupChatRoomCreateRequestDto.getChatRoomName())
        .chatRoomCount(1)
        .chatRoomCountMax(groupChatRoomCreateRequestDto.getChatRoomCountMax())
        .chatRoomType(ChatRoomType.GROUP_CHATROOM)
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
    Optional<ChatRoom> chatRoom=chatRoomRepository.findById(chatRoomId);

    if(!chatRoom.isPresent()){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }

    chatRoomRepository.delete(chatRoom.get());
  }
}
