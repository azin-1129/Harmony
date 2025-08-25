package com.harmony.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisMessageListener {
  private static final Map<String, ChannelTopic> TOPICS=new HashMap<>();

  private final RedisMessageListenerContainer redisMessageListenerContainer;
  private final RedisSubscriber redisSubscriber;

  public void enterChatRoom(String chatRoomId){
    System.out.println(TOPICS);
    ChannelTopic topic=getTopic(chatRoomId);
    if(topic==null){ // topic이 없다면 새로운 topic을 발행해 저장
      topic=new ChannelTopic(chatRoomId);
      redisMessageListenerContainer.addMessageListener(redisSubscriber, topic);
      TOPICS.put(chatRoomId, topic);
    }
  }

  public ChannelTopic getTopic(String chatRoomId){
    return TOPICS.get(chatRoomId);
  }
}
