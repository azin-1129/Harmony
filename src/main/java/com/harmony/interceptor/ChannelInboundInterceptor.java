package com.harmony.interceptor;

import static com.harmony.security.filter.AuthenticationFilter.BEARER_PREFIX;

import com.harmony.exception.InvalidTokenException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.security.utils.TokenProvider;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Order(Ordered.HIGHEST_PRECEDENCE+99) // 필요한가?
@Slf4j
@RequiredArgsConstructor
@Component
public class ChannelInboundInterceptor implements ChannelInterceptor {
  private final TokenProvider tokenProvider;

  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor header = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    System.out.println("command:" + header.getCommand());

    switch (header.getCommand()) {
      case CONNECT:
        // Spring STOMP Docs에 따르면, CONNECT 시 Header를 인증한다.
        log.info("해당 커맨드는 CONNECT 입니다.");
        // 사람들은 ChannelInterceptor에서 JWT 인증을 수행하고, access.user에 등록한다.
        //
        String accessToken=header.getFirstNativeHeader("Authorization");
        if(accessToken.startsWith(BEARER_PREFIX) && accessToken.length()>7){
            accessToken=accessToken.substring(7);
        }else{
          throw new InvalidTokenException(
              ErrorCode.AUTH_INVALID_TOKEN
          );
        }

        log.info("access token:"+accessToken);
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        log.info("get Authentication에 성공했습니다.");

        header.setUser(authentication);
      case SUBSCRIBE:
        log.info("Subscribed to channel");
        break;
      case UNSUBSCRIBE:
        log.info("Unsubscribed from channel");
        break;
    }

    log.info("destination:"+header.getDestination());

    return message;
  }
}