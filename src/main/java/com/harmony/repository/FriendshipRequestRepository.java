package com.harmony.repository;

import com.harmony.entity.FriendshipRequest;
import com.harmony.entity.FriendshipRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
  @Query("SELECT f FROM FriendshipRequest f " +
      "WHERE f.friendshipRequestSender.userId = :fromUserId " +
      "AND f.friendshipRequestReceiver.userId = :toUserId "+
  "AND f.friendshipRequestStatus = :status")
  Optional<FriendshipRequest> findByToUserIdAndFromUserIdAndFriendshipRequestStatus(@Param("fromUserId") Long fromUserId,
      @Param("toUserId") Long toUserId, @Param("status") FriendshipRequestStatus status);

  @Query("SELECT f FROM FriendshipRequest f " +
      "WHERE f.friendshipRequestSender.userId = :fromUserId " +
      "AND f.friendshipRequestStatus = :status")
  List<FriendshipRequest> findByFromUserIdAndFriendshipReqStatus(Long fromUserId, FriendshipRequestStatus status);


}
