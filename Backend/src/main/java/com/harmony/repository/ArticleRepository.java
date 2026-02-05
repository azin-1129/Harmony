package com.harmony.repository;

import com.harmony.entity.Article;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
  @Query("SELECT a FROM Article a WHERE a.author.userId IN (" +
      "SELECT f.friend.userId FROM Friendship f WHERE f.user.userId = :userId AND f.friend.withdraw=false)"
      + "ORDER BY a.createdAt DESC")
  List<Article> findArticlesByFriendsOfUserOrderByCreatedAtDesc(@Param("userId") Long userId);

  @Query("SELECT a FROM Article a WHERE a.author.userId = :authorId ORDER BY a.createdAt DESC")
  List<Article> findArticlesByUserIdOrderByCreatedAtDesc(@Param("authorId") Long authorId);

  @Query("SELECT a FROM Article a WHERE a.articleId = :articleId")
  Optional<Article> findArticleByArticleId(@Param("articleId") Long articleId);

  @Query("SELECT a FROM Article a WHERE a.author.userId = :authorId")
  List<Article> findArticlesByAuthorId(@Param("authorId") Long authorId);

  List<Article> findByIsDeleted(Boolean isDeleted);

  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("DELETE FROM Article a WHERE a.articleId in :ids")
  void deleteArticlesBulk(@Param("ids") List<Long> ids);

  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("UPDATE Article a SET a.articleCommentCount=a.articleCommentCount-1 WHERE a.articleId in :ids")
  void updateArticleCommentCountsBulk(@Param("ids") List<Long> ids);

  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("UPDATE Article a SET a.articleLikeCount=a.articleLikeCount-1 WHERE a.articleId in :ids")
  void updateArticleLikeCountsBulk(@Param("ids") List<Long> ids);
}
