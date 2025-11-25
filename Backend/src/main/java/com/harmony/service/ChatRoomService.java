package com.harmony.service;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
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
  // TODO: 중복 처리는 Participants 조인해서 확인해야할 듯
  // 개인챗방 생성
  public ChatRoom createPersonalChatRoom() {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName("")
        .chatRoomCount(2)
        .chatRoomCountMax(2)
        .chatRoomType(ChatRoomType.PERSONAL_CHATROOM)
        .build();

    chatRoomRepository.save(chatRoom);

    return chatRoom;
  }

  // 단체챗방 생성
  // TODO: 만든 사람 참가 시켜야됨
  public ChatRoom createGroupChatRoom(
      CreateAndParticipateGroupChatRoomRequestDto groupChatRoomCreateRequestDto) {
    ChatRoom chatRoom = ChatRoom.builder()
        .chatRoomName(groupChatRoomCreateRequestDto.getChatRoomName())
        .chatRoomCount(1)
        .chatRoomCountMax(groupChatRoomCreateRequestDto.getChatRoomCountMax())
        .chatRoomType(ChatRoomType.GROUP_CHATROOM)
        .build();

    chatRoomRepository.save(chatRoom);

    return chatRoom;
  }

  public Optional<ChatRoom> getChatRoomById(Long chatRoomId) {
    return chatRoomRepository.findById(chatRoomId);
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
