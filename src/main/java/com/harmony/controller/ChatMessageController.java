package com.harmony.controller;

import com.harmony.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ChatMessageController {
  private final SimpMessageSendingOperations template;

  @MessageMapping("/chat/{chatRoomId}")
  public void sendMessage(@RequestBody ChatMessageDto chatMessageDto){
    System.out.println(chatMessageDto);
    template.convertAndSend("/sub/chat/"+chatMessageDto.getChatRoomId(), chatMessageDto);
  }
}
