package com.harmony.service;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ChatMessageService {
  private final ChatMessageRepository chatMessageRepository;

  public List<SelectChatRoomMessagesResponseDto> selectChatRoomMessages(Long chatRoomId){
    // TODO: 채팅방이 있는지부터 판단좀
    List<ChatMessage> messages= chatMessageRepository.findByChatRoomId(chatRoomId);

    List<SelectChatRoomMessagesResponseDto> selectChatRoomMessagesResponseDtos=new ArrayList<>();
    for(ChatMessage message:messages){
      SelectChatRoomMessagesResponseDto selectChatRoomMessagesResponseDto=SelectChatRoomMessagesResponseDto.builder()
          .chatMessageType(message.getChatMessageType())
          .senderId(message.getParticipant().getUser().getUserId())
          .chatMessage(message.getChatMessage())
          .build();
      selectChatRoomMessagesResponseDtos.add(selectChatRoomMessagesResponseDto);
    }

    return selectChatRoomMessagesResponseDtos;
  }
}
