package com.harmony.service;

import com.harmony.dto.request.ChatMessageRequestDto;
import com.harmony.dto.response.ChatMessageResponseDto;
import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ChatMessageRepository;
import com.harmony.repository.ChatRoomRepository;
import com.harmony.repository.ParticipantRepository;
import com.harmony.repository.UserRepository;
import com.harmony.security.impl.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ChatMessageService {
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final SimpMessageSendingOperations template;
  private final ParticipantRepository participantRepository;

  @Cacheable(value="chatMessages", key="#chatRoomId+':'+#chatMessageId")
  public SlicedResponseDto<SelectChatRoomMessagesResponseDto> selectChatRoomMessages(Long chatRoomId, Long chatMessageId, Pageable pageable){
    // TODO: 채팅방이 있는지부터 판단좀
    return chatMessageRepository.selectChatMessageSliceOrderByIdDesc(chatRoomId, chatMessageId, pageable);
  }

  public void sendAndSaveChatData(ChatMessageRequestDto chatMessageRequestDto, Long chatRoomId, Authentication authentication){
    // TODO: 상세로직 service 코드로 이동
    UserDetailsImpl sender=((UserDetailsImpl)authentication.getPrincipal());

    log.info("전송중인 사용자의 정보:"+sender);
    // TODO: 입장 메세지는?
    log.info(chatMessageRequestDto.toString());

    ChatRoom chatRoom=chatRoomRepository.findById(chatRoomId)
        .orElseThrow(()->
            new EntityNotFoundException(
                ErrorCode.CHATROOM_NOT_FOUND
            )
        );

    ChatMessageResponseDto chatMessageResponseDto=ChatMessageResponseDto.builder()
        .chatRoomId(chatRoomId)
        .chatMessageType(chatMessageRequestDto.getChatMessageType())
        .senderId(sender.getUserId())
        .chatMessage(chatMessageRequestDto.getChatMessage())
        .build();

    template.convertAndSend("/sub/"+chatRoomId, chatMessageResponseDto);

    ParticipantId participantSenderId = ParticipantId.builder()
        .userId(sender.getUserId())
        .chatroomId(chatRoomId)
        .build();
    Participant participantUser=participantRepository.findByParticipantId(participantSenderId);

    ChatMessage chatMessage=ChatMessage.builder()
        .participant(participantUser)
        .chatMessageType(chatMessageRequestDto.getChatMessageType())
        .chatMessage(chatMessageRequestDto.getChatMessage())
        .isRead(false)
        .build();

    chatMessageRepository.save(chatMessage);
    chatRoom.updateLastChatMessageId(chatMessage.getChatMessageId());
  }
}
