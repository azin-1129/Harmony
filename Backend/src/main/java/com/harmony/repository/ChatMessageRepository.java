package com.harmony.repository;

import com.harmony.entity.ChatMessage;
import com.harmony.repository.querydsl.DslChatMessageRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>,
    DslChatMessageRepository {
  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("DELETE FROM ChatMessage cm WHERE cm.participant.chatRoom.chatRoomId in :ids")
  void deleteChatMessagesBulk(@Param("ids") List<Long> ids);
}
