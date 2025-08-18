package com.harmony.repository.querydsl;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import org.springframework.data.domain.Pageable;

public interface DslChatMessageRepository {
  SlicedResponseDto<SelectChatRoomMessagesResponseDto> selectChatMessageSliceOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
}
