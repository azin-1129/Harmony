package com.harmony.repository;

import com.harmony.entity.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
  @Query("SELECT b FROM Block b WHERE b.blockingUser.userId= :blockingId")
  List<Block> findByBlockingId(@Param("blockingId") Long blockingId);
  @Query("SELECT b FROM Block b WHERE b.blockingUser.userId=:blockingId AND b.blockedUser.userId=:blockedId")
  Optional<Block> findByBlockingIdAndBlockedId(@Param("blockingId") Long blockingId, @Param("blockedId") Long blockedId);
}
