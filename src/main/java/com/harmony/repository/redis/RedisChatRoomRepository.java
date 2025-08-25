package com.harmony.repository.redis;

import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.entity.ChatRoom;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisChatRoomRepository {
  // Key: CHAT_ROOM:{chatRoomId}
  private static final String CHAT_ROOM="CHAT_ROOM";
  private static final String CHAT_MESSAGE="CHAT_MESSAGE";
  private static final String DELIMITER=":";

  private final RedisTemplate<String, Object> redisTemplate;

  private HashOperations<String, String, ChatRoom> opsHashChatRoom;
  private HashOperations<String, String, List<ChatMessage>> opsHashChatMessage;

  @PostConstruct
  private void init(){
    opsHashChatRoom = redisTemplate.opsForHash();
    opsHashChatMessage = redisTemplate.opsForHash();
  }

  public void createChatRoom(ChatRoom chatRoom){
    log.info("채팅방을 추가하고 있어요!");
    opsHashChatRoom.put(CHAT_ROOM, chatRoom.getChatRoomId().toString(), chatRoom);
    System.out.println(opsHashChatRoom.values(CHAT_ROOM));
  }

  // TODO: ChatRoom들 변환
  public List<ChatRoom> findAllChatRoom(){
    return opsHashChatRoom.values(CHAT_ROOM);
  }

  public ChatRoom findChatRoomById(String chatRoomId){
    return opsHashChatRoom.get(CHAT_ROOM, chatRoomId);
  }

  public void saveChatMessage(String chatRoomId, ChatMessage chatMessage){
    List<ChatMessage> messages=opsHashChatMessage.get(CHAT_MESSAGE, chatRoomId);
    if(messages==null){
      messages=new ArrayList<>();
    }
    messages.add(chatMessage);
    opsHashChatMessage.put(CHAT_MESSAGE, chatRoomId, messages);
  }

  public List<SelectChatRoomMessagesResponseDto> findChatMessageByChatRoomId(String chatRoomId){
    List<ChatMessage> chatMessages=opsHashChatMessage.get(CHAT_MESSAGE, chatRoomId);
    List<SelectChatRoomMessagesResponseDto> selectChatRoomMessagesResponseDtos=new ArrayList<>();
    if(chatMessages==null){
      return selectChatRoomMessagesResponseDtos;
    }
    for(ChatMessage chatMessage:chatMessages){
      SelectChatRoomMessagesResponseDto selectChatRoomMessagesResponseDto=SelectChatRoomMessagesResponseDto.builder()
          .chatMessageType(chatMessage.getChatMessageType())
          .senderId(chatMessage.getParticipant().getUser().getUserId())
          .chatMessage(chatMessage.getChatMessage())
          .createdAt(chatMessage.getCreatedAt())
          .build();
      selectChatRoomMessagesResponseDtos.add(selectChatRoomMessagesResponseDto);
    }
    return selectChatRoomMessagesResponseDtos;
  }
}
