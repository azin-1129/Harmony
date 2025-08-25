package com.harmony.service;

import com.harmony.dto.response.ChatMessageResponseDto;
import com.harmony.dto.response.SelectChatRoomMessagesResponseDto;
import com.harmony.entity.ChatMessage;
import com.harmony.entity.ChatRoom;
import com.harmony.repository.redis.RedisChatRoomRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Service
public class RedisChatService {
  private final RedisPublisher redisPublisher;
  private final RedisMessageListener redisMessageListener;
  private final RedisChatRoomRepository redisChatRoomRepository;

  public void send(ChatMessageResponseDto chatMessageResponseDto, String chatRoomId, ChatMessage chatMessage) {
    redisPublisher.publish(redisMessageListener.getTopic(chatRoomId), chatMessageResponseDto);
    redisChatRoomRepository.saveChatMessage(chatRoomId, chatMessage); // Redis에 Message 저장
  }

  public void createChatRoom(ChatRoom chatRoom) {
    redisChatRoomRepository.createChatRoom(chatRoom);
  }

  public void enterChatRoom(String chatRoomId){
    redisMessageListener.enterChatRoom(chatRoomId);
  }

  public List<ChatRoom> getChatRooms(){
    return redisChatRoomRepository.findAllChatRoom();
  }

  // 사용자가 속한 채팅방 목록
  public ChatRoom getChatRooms(String chatRoomId){
    return redisChatRoomRepository.findChatRoomById(chatRoomId);
  }

  public List<SelectChatRoomMessagesResponseDto> getChatMessages(String chatRoomId){
    return redisChatRoomRepository.findChatMessageByChatRoomId(chatRoomId);
  }
}
