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

  Optional<User> findByEmail(String email);
  Optional<User> findByUserIdentifier(String userIdentifier);
  Optional<User> findByNickname(String nickname);
  List<User> findByWithdrawnAtBefore(LocalDateTime cutoffDate);

  @Transactional
  @Modifying(clearAutomatically=true)
  @Query("UPDATE User u SET u.email=null, u.userIdentifier=null, u.nickname=null WHERE u.userId in :ids")
  void deleteWithdrawnUserInfoBulk(@Param("ids") List<Long> ids);
}
