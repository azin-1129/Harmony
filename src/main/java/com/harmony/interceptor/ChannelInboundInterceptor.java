package com.harmony.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelInboundInterceptor implements ChannelInterceptor {

  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor header = StompHeaderAccessor.wrap(message);

    System.out.println("command:" + header.getCommand());

    switch (header.getCommand()) {
      case SUBSCRIBE:
        log.info("Subscribed to channel");
        break;
      case UNSUBSCRIBE:
        log.info("Unsubscribed from channel");
    }

    log.info("destination:"+header.getDestination());

    return message;
  }
}