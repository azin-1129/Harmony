package com.harmony.repository;

import com.harmony.entity.FriendshipRequest;
import com.harmony.entity.FriendshipRequestStatus;
import com.harmony.repository.querydsl.DslFriendshipRequestRepository;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long>,
    DslFriendshipRequestRepository {
  @Query("SELECT f FROM FriendshipRequest f " +
      "WHERE f.friendshipRequestSender.userId = :fromUserId " +
      "AND f.friendshipRequestReceiver.userId = :toUserId "+
  "AND f.friendshipRequestStatus = :status")
  Optional<FriendshipRequest> findByFromUserIdAndToUserIdAndFriendshipRequestStatus(@Param("fromUserId") Long fromUserId,
      @Param("toUserId") Long toUserId, @Param("status") FriendshipRequestStatus status);

  @Query("SELECT f FROM FriendshipRequest f " +
      "WHERE f.friendshipRequestSender.userId = :fromUserId " +
      "AND f.friendshipRequestStatus = :status")
  List<FriendshipRequest> findByFromUserIdAndFriendshipRequestStatus(@Param("fromUserId") Long fromUserId, @Param("status") FriendshipRequestStatus status);

  @Query("SELECT fr FROM FriendshipRequest fr WHERE fr.friendshipRequestSender.userId = :userId OR fr.friendshipRequestReceiver.userId = :userId")
  List<FriendshipRequest> findFriendshipRequestsByUserId(@Param("userId") Long userId);

  @Transactional
  @Modifying(clearAutomatically=true)
  @Query("DELETE FROM FriendshipRequest fr WHERE fr.friendshipRequestSender.userId in :ids OR fr.friendshipRequestReceiver.userId in :ids")
  void deleteFriendshipRequestBulk(@Param("ids") List<Long> ids);
}
