package com.harmony.service;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import com.harmony.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ChatMessageService {
  private final ChatMessageRepository chatMessageRepository;

  public SlicedResponseDto<SelectChatRoomMessagesResponseDto> selectChatRoomMessages(Long chatRoomId, Pageable pageable){
    // TODO: 채팅방이 있는지부터 판단좀
    return chatMessageRepository.selectChatMessageSliceOrderByCreatedAtDesc(chatRoomId, pageable);
  }
}
