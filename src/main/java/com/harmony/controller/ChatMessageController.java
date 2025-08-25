package com.harmony.controller;

import com.harmony.dto.request.ChatMessageRequestDto;
import com.harmony.dto.response.ChatMessageResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.repository.ChatMessageRepository;
import com.harmony.repository.ChatRoomRepository;
import com.harmony.repository.UserRepository;
import com.harmony.security.impl.UserDetailsImpl;
import com.harmony.service.ChatMessageService;
import com.harmony.service.RedisChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatMessageController {
//  private final RedisChatService redisChatMessageService;
  private final SimpMessageSendingOperations template;
  private final ChatMessageService chatMessageService;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;

  @MessageMapping("/{chatRoomId}")
  public void sendMessage(@RequestBody ChatMessageRequestDto chatMessageRequestDto, @DestinationVariable Long chatRoomId, Authentication authentication) {
    UserDetailsImpl sender=((UserDetailsImpl)authentication.getPrincipal());
    log.info("전송중인 사용자의 정보:"+sender);
    // TODO: 입장 메세지는?
    log.info(chatMessageRequestDto.toString());

    ChatMessageResponseDto chatMessageResponseDto=ChatMessageResponseDto.builder()
            .chatRoomId(chatRoomId)
                .chatMessageType(chatMessageRequestDto.getChatMessageType())
                    .senderId(sender.getUserId())
                        .chatMessage(chatMessageRequestDto.getChatMessage())
                            .build();

    template.convertAndSend("/sub/"+chatRoomId, chatMessageResponseDto);

    // TODO: 일단 저장 성능 측정, 후 캐싱 처리
    ParticipantId participantSenderId = ParticipantId.builder()
        .userId(sender.getUserId())
        .chatroomId(chatRoomId)
        .build();

    // TODO: 채팅방 부재 시 예외
    Participant participantUser=Participant.builder()
        .participantId(participantSenderId)
        .user(userRepository.findById(sender.getUserId()).orElseThrow(
            ()->new EntityNotFoundException(
                ErrorCode.USER_NOT_FOUND
            )
        ))
        .chatRoom(chatRoomRepository.findById(chatRoomId).get())
        .build();

    ChatMessage chatMessage=ChatMessage.builder()
            .participant(participantUser)
            .chatMessageType(chatMessageRequestDto.getChatMessageType())
            .chatMessage(chatMessageRequestDto.getChatMessage())
            .isRead(false)
            .build();

//    redisChatMessageService.send(chatMessageResponseDto, chatRoomId.toString(), chatMessage);
    chatMessageRepository.save(chatMessage);
  }

  // 특정 채팅방의 메시지 조회
  @GetMapping("/{chatRoomId}")
  public ResponseEntity<Object> selectChatRoomMessages(@PathVariable Long chatRoomId,
                                                       @RequestParam(required = false) Long chatMessageId,
                                                       @SortDefault(sort="createdAt", direction= Sort.Direction.DESC) Pageable pageable){
    return SuccessResponse.createSuccess(
        SuccessCode.CHAT_MESSAGE_READ_SUCCESS,
        chatMessageService.selectChatRoomMessages(chatRoomId, chatMessageId, pageable)
    );
  }
}
