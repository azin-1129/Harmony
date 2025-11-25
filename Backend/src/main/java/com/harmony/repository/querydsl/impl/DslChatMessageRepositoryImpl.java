package com.harmony.repository.querydsl.impl;

import static com.harmony.entity.QChatMessage.chatMessage1;
import static com.harmony.entity.QChatRoom.chatRoom;
import static com.harmony.entity.QParticipant.participant;
import static com.harmony.entity.QUser.user;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.repository.querydsl.DslChatMessageRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DslChatMessageRepositoryImpl implements DslChatMessageRepository {
  private final JPAQueryFactory qf;

  @Override
  public SlicedResponseDto<SelectChatRoomMessagesResponseDto> selectChatMessageSliceOrderByIdDesc(Long chatRoomId, Long chatMessageId, Pageable pageable) {
    // 해당 채팅방 ID의 메시지 불러오기
    List<ChatMessage> chatMessages=qf
        .selectDistinct(chatMessage1)
        .from(chatMessage1)
        .where(chatMessage1.participant.chatRoom.chatRoomId.eq(chatRoomId), messageIdLt(chatMessageId))
        .join(chatMessage1.participant, participant).fetchJoin()
        .join(participant.chatRoom, chatRoom).fetchJoin()
        .join(participant.user, user).fetchJoin()
        .orderBy(chatMessage1.chatMessageId.desc())
        .limit(pageable.getPageSize()+1)
        .fetch();

    // cursor 역할을 하는 chat message ID로부터 limit n개 가져오기
    // ID 기반 DESC 정렬
    List<SelectChatRoomMessagesResponseDto> selectChatRoomMessagesResponseDtos=new ArrayList<>();
    for(ChatMessage message:chatMessages){
      SelectChatRoomMessagesResponseDto selectChatRoomMessagesResponseDto=SelectChatRoomMessagesResponseDto.builder()
          .chatMessageId(message.getChatMessageId()) // test
          .chatMessageType(message.getChatMessageType())
          .senderId(message.getParticipant().getUser().getUserId())
          .chatMessage(message.getChatMessage())
          .createdAt(message.getCreatedAt())
          .build();
      selectChatRoomMessagesResponseDtos.add(selectChatRoomMessagesResponseDto);
    }

    Long lastChatMessageId=
        chatMessages.size()>pageable.getPageSize()-1 ?
            chatMessages.get(pageable.getPageSize()-1).getChatMessageId() : null;

    boolean hasNext=false;
    if(chatMessages.size()>pageable.getPageSize()){
      chatMessages.subList(0, pageable.getPageSize());
      hasNext=true;
    }

    return new SlicedResponseDto<SelectChatRoomMessagesResponseDto>(lastChatMessageId, selectChatRoomMessagesResponseDtos, hasNext);
  }
  private BooleanExpression messageIdLt(Long chatMessageId) {
    return chatMessageId!=null ? chatMessage1.chatMessageId.lt(chatMessageId) : null;
  }
}
