package com.harmony.global.config;

import com.harmony.global.response.handler.StompExceptionHandler;
import com.harmony.interceptor.ChannelInboundInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {
  private final StompExceptionHandler stompExceptionHandler;
  private final ChannelInboundInterceptor channelInboundInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/sub"); // 메시지 구독 요청(수신). Subscription URL prefixes
    config.setApplicationDestinationPrefixes("/pub"); // 메시지 발행 요청(송신)
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*");
    registry.setErrorHandler(stompExceptionHandler);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInboundInterceptor);
  }
}
