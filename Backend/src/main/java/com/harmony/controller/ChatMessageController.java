package com.harmony.controller;

import com.harmony.dto.request.ChatMessageRequestDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
  private final ChatMessageService chatMessageService;

  @MessageMapping("/{chatRoomId}")
  public void sendMessage(@RequestBody ChatMessageRequestDto chatMessageRequestDto, @DestinationVariable Long chatRoomId, Authentication authentication) {
    chatMessageService.sendAndSaveChatData(chatMessageRequestDto, chatRoomId, authentication);
  }

  // 특정 채팅방의 메시지 조회
  @GetMapping("/{chatRoomId}")
  public ResponseEntity<Object> selectChatRoomMessages(@PathVariable Long chatRoomId,
                                                       @RequestParam(required = false) Long chatMessageId,
                                                       @SortDefault(sort="createdAt", direction= Sort.Direction.DESC) Pageable pageable){
    log.info("페이지 넘버:"+pageable.getPageNumber());
    return SuccessResponse.createSuccess(
        SuccessCode.CHAT_MESSAGE_READ_SUCCESS,
        chatMessageService.selectChatRoomMessages(chatRoomId, chatMessageId, pageable)
    );
  }
}
