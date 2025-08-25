package com.harmony.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.dto.response.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
  private final SimpMessageSendingOperations messagingTemplate;
  private final RedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  // Redis에서 메시지가 publish될 때까지 대기하다가, 메시지가 발행되면 처리.
  // publish된 메시지를 변환하고, messagingTemplate를 이용해 subscribe 측에 전송
  @Override
  public void onMessage(Message message, byte[] pattern){
    try{
      String publishedMessage=(String) redisTemplate.getStringSerializer().deserialize(message.getBody());
      // TODO: publish할 때, ChatMessage? or ChatMessageResponseDto? 일괄 DB로 저장할 때 어떻게 해야 됨
      ChatMessageResponseDto chatMessageResponseDto=objectMapper.readValue(publishedMessage, ChatMessageResponseDto.class);
      messagingTemplate.convertAndSend("/chat/"+chatMessageResponseDto.getChatRoomId(), chatMessageResponseDto);
      log.info("Message [{}] send by member: {} to chatting room: {}", chatMessageResponseDto.getChatMessage(), chatMessageResponseDto.getSenderId(), chatMessageResponseDto.getChatRoomId());
    }catch(Exception e){
      log.error(e.getMessage());
    }
  }
}
