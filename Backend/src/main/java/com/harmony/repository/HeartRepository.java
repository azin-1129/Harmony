package com.harmony.repository;

import com.harmony.entity.Heart;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
  @Query("SELECT h FROM Heart h WHERE h.user.userId= :userId AND h.article.articleId= :articleId")
  Optional<Heart> findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

  @Query("SELECT h FROM Heart h WHERE h.user.userId=:userId")
  List<Heart> findHeartsByUserId(@Param("userId") Long userId);

  @Query("SELECT h FROM Heart h WHERE h.article.articleId=:articleId")
  List<Heart> findHeartsByArticleId(@Param("articleId") Long articleId);

  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("DELETE FROM Heart h WHERE h.heartId in :ids")
  void deleteHeartsBulk(@Param("ids") List<Long> ids);
}
