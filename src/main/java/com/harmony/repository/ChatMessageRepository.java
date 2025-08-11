package com.harmony.repository;

import com.harmony.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  @Query("SELECT cm FROM ChatMessage cm WHERE cm.participant.chatRoom.chatRoomId=:chatRoomId")
  List<ChatMessage> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
