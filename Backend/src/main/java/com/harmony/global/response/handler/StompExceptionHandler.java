package com.harmony.global.response.handler;

import com.harmony.exception.InvalidTokenException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {
  @Override
  public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex){
    Throwable exception=ex;
    if(exception instanceof InvalidTokenException){
      return handleInvalidTokenException((InvalidTokenException) exception);
    }
    return super.handleClientMessageProcessingError(clientMessage, ex);
  }

  private Message<byte[]> handleInvalidTokenException(InvalidTokenException ex){
    return prepareErrorMessage(ex.getResponseCode().getMessage());
  }

  private Message<byte[]> prepareErrorMessage(String errorMessage){
    log.info("Stomp Exception Handler에서 메시지를 준비하고 있습니다:{}", errorMessage);
    String message=String.valueOf(errorMessage);
    StompHeaderAccessor accessor=StompHeaderAccessor.create(StompCommand.ERROR);

    accessor.setMessage(message);
    accessor.setLeaveMutable(true);

    return MessageBuilder.createMessage(message.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
  }
}
