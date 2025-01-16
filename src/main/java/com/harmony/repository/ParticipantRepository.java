package com.harmony.repository;

import com.harmony.entity.ChatRoomType;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
  @Query("SELECT cr.chatRoomId FROM ChatRoom cr WHERE cr.chatRoomType = :chatRoomType " +
      "AND cr.chatRoomId IN " +
      "(SELECT p1.participantId.chatroomId FROM Participant p1 WHERE p1.participantId.userId = :userId) " +
      "AND cr.chatRoomId IN " +
      "(SELECT p2.participantId.chatroomId FROM Participant p2 WHERE p2.participantId.userId = :partnerId)")
  Optional<Long> findPersonalChatRoomIdByParticipant(@Param("chatRoomType") ChatRoomType chatRoomType, @Param("userId") Long userId, @Param("partnerId") Long partnerId);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Participant p WHERE p.participantId.userId = :userId AND p.participantId.chatroomId = :chatroomId")
  Boolean existsByUserIdAndChatroomId(@Param("userId") Long userId, @Param("chatroomId") Long chatroomId);
}
// 개인 채팅방이 이미 있는지 확인하는 쿼리
/**
 * 나, 상대 id 기반으로 참여한 id 중 일치하는 값이 있으며, 그 값이 개인 채팅방인지 조회해야 함
 *
 */