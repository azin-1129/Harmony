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
    int pageSize=pageable.getPageSize();
    // 해당 채팅방 ID의 메시지 불러오기
    List<ChatMessage> chatMessages=qf
        .selectDistinct(chatMessage1)
        .from(chatMessage1)
        .where(chatMessage1.participant.chatRoom.chatRoomId.eq(chatRoomId), messageIdLt(chatMessageId))
        .join(chatMessage1.participant, participant).fetchJoin()
        .join(participant.chatRoom, chatRoom).fetchJoin()
        .join(participant.user, user).fetchJoin()
        .orderBy(chatMessage1.chatMessageId.desc())
        .limit(pageSize+1)
        .fetch();

    boolean hasNext=false;
    int chatMessagesSize=chatMessages.size();
    Long lastChatMessageId=null;

    if(chatMessagesSize>pageSize){
      lastChatMessageId=chatMessages.get(pageSize).getChatMessageId();
      System.out.println("조회된 메시지가 페이징 사이즈보다 많아 절단했습니다.");
      chatMessages=chatMessages.subList(0, pageable.getPageSize());
      hasNext=true;
    }

    // cursor 역할을 하는 chat message ID로부터 limit n개 가져오기
    // ID 기반 DESC 정렬
    List<SelectChatRoomMessagesResponseDto> selectChatRoomMessagesResponseDtos=new ArrayList<>();
    for(ChatMessage chatMessage:chatMessages){
      SelectChatRoomMessagesResponseDto selectChatRoomMessagesResponseDto=SelectChatRoomMessagesResponseDto.builder()
          .chatMessageId(chatMessage.getChatMessageId()) // test
          .chatMessageType(chatMessage.getChatMessageType())
          .senderId(chatMessage.getParticipant().getUser().getUserId())
          .chatMessage(chatMessage.getChatMessage())
          .createdAt(chatMessage.getCreatedAt())
          .build();
      selectChatRoomMessagesResponseDtos.add(selectChatRoomMessagesResponseDto);
    }

    return new SlicedResponseDto<SelectChatRoomMessagesResponseDto>(lastChatMessageId, selectChatRoomMessagesResponseDtos, hasNext);
  }
  private BooleanExpression messageIdLt(Long chatMessageId) {
    return chatMessageId!=null ? chatMessage1.chatMessageId.loe(chatMessageId) : null;
  }
}
