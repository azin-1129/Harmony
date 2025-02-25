package com.harmony.repository;

import com.harmony.entity.Friendship;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
  @Query("SELECT f FROM Friendship f WHERE f.user.userId = :userId")
  List<Friendship> findByUserId(@Param("userId") Long userId);

  @Query("SELECT f FROM Friendship f WHERE f.user.userId = :userId AND f.friend.userId = :friendId")
  Optional<Friendship> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

}
