package com.harmony.repository;

import com.harmony.entity.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUserId(Long userId);
  Optional<User> findByEmail(String email);
  Optional<User> findByUserIdentifier(String userIdentifier);
  Optional<User> findByNickname(String nickname);
  List<User> findByWithdrawnAtBefore(LocalDateTime cutoffDate);
  List<User> findByWithdraw(Boolean withdraw);
  List<User> findByNicknameContainsOrUserIdentifierContains(String nickname, String userIdentifier);

  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  boolean existsByUserIdentifier(String userIdentifier);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.nickname=null, u.userIdentifier=null, u.profileImageName=null, u.email=null  WHERE u.userId IN :ids")
  void updateWithdrawnUserBulk(@Param("ids") List<Long> ids);

  @Transactional
  @Modifying(clearAutomatically=true)
  @Query("DELETE FROM User u WHERE u.userId in :ids")
  void deleteWithdrawnUserBulk(@Param("ids") List<Long> ids);
}
