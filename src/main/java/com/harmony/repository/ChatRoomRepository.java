package com.harmony.repository;

import com.harmony.entity.ChatRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query("update ChatRoom c set c.chatRoomCount=c.chatRoomCount-1 where c.chatRoomId in :chatRoomIds")
    void minusChatRoomCountBulk(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("SELECT c FROM ChatRoom c WHERE c.chatRoomCount=0")
    List<ChatRoom> findAllZeroCountChatRooms();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChatRoom c WHERE c.chatRoomId in :ids")
    void deleteChatRoomsBulk(@Param("ids") List<Long> ids);
}
