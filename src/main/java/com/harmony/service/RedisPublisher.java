package com.harmony.service;

import com.harmony.dto.response.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {
  private final RedisTemplate<String, Object> redisTemplate;

  public void publish(ChannelTopic channelTopic, ChatMessageResponseDto chatMessageResponseDto) {
    System.out.println("퍼블리싱 메시지:"+chatMessageResponseDto);
    redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessageResponseDto);
  }
}
