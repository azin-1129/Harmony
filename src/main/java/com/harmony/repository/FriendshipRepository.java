package com.harmony.repository;

import com.harmony.entity.Friendship;
import com.harmony.repository.querydsl.DslFriendshipRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long>,
    DslFriendshipRepository {
  @Query("SELECT f FROM Friendship f WHERE f.user.userId = :userId")
  List<Friendship> findByUserId(@Param("userId") Long userId);

  @Query("SELECT f FROM Friendship f WHERE f.user.userId = :userId AND f.friend.userId = :friendId")
  Optional<Friendship> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

  @Query("SELECT COUNT(f) > 0 from Friendship f where f.user.userId= :userId AND f.friend.userId = :friendId")
  boolean existsByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
