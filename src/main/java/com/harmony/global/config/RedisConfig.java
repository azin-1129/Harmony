package com.harmony.global.config;

import com.harmony.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
  private static final String TOPIC_NAME="chat";

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

//  @Value("${spring.data.redis.password}")
//  private String password;

  // Redis Server와 상호작용하기 위한 RedisTemplate
  @Bean
  public RedisTemplate<String, Object> redisTemplate(){
    RedisTemplate<String, Object> redisTemplate=new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    // Redis 서버는 Byte 코드만 저장되므로 key, value에 Serializer 설정
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class)); // JSON 형식으로 메시지 교환
    return redisTemplate;
  }

  // Published Message 처리를 위한 Listener
  // ChannelTopic으로부터 Message를 받고, 주입된 Listener들에게 비동기적으로 전송
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory,
      MessageListenerAdapter listenerAdapter,
      ChannelTopic channelTopic){
    RedisMessageListenerContainer container=new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter, channelTopic);
    return container;
  }

  // Netty(비동기 이벤트 기반 고성능 네트워크 프레임워크) 기반의 Redis Client LettuceConnectionFactory Bean 등록
  @Bean
  public RedisConnectionFactory redisConnectionFactory(){
    RedisStandaloneConfiguration redisStandaloneConfiguration=new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
//    redisStandaloneConfiguration.setPassword(password);
    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  // RedisMessageListenerContainer로부터 메시지를 전달받고, 실제 메시지를 처리하는 Subscriber 추가
  @Bean
  public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber){
    return new MessageListenerAdapter(subscriber, "onMessage");
  }

  // Topic 공유를 위해 Bean 등록
  @Bean
  public ChannelTopic channelTopic(){
    return new ChannelTopic(TOPIC_NAME);
  }
}