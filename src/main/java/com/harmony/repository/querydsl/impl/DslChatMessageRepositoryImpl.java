package com.harmony.repository.querydsl.impl;

import static com.harmony.entity.QChatMessage.chatMessage1;
import static com.harmony.entity.QChatRoom.chatRoom;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.repository.querydsl.DslChatMessageRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DslChatMessageRepositoryImpl implements DslChatMessageRepository {
  private final JPAQueryFactory qf;


  // TODO: 쿼리 한 번 더 나가는 것 수정
  @Override
  public SlicedResponseDto<SelectChatRoomMessagesResponseDto> selectChatMessageSliceOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable) {
    // Join 해야함
    // 해당 채팅방 ID의 메시지 불러오기
    List<ChatMessage> chatMessages=qf
        .select(chatMessage1)
        .from(chatRoom)
        .join(chatMessage1)
        .on(chatRoom.chatRoomId.eq(chatMessage1.participant.chatRoom.chatRoomId))
        .where(chatRoom.chatRoomId.eq(chatRoomId))
        .orderBy(chatMessage1.chatMessageId.desc())
        .limit(pageable.getPageSize()+1)
        .fetch();

//        .where(chatMessage1.chatMessageId.eq(chatMessageId))
//        .orderBy(chatMessage1.chatMessageId.desc())
//        .limit(pageable.getPageSize()+1)
//        .fetch();
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
}
