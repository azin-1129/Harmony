package com.harmony.repository;

import com.harmony.entity.Heart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
  @Query("SELECT h FROM Heart h WHERE h.user.userId= :userId AND h.article.articleId= :articleId")
  Optional<Heart> findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
