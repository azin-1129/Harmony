package com.harmony.exception;

import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.BusinessException;

public class GroupChatRoomFullException extends BusinessException {
  public GroupChatRoomFullException(ErrorCode errorCode) {
    super(errorCode);
  }
}
