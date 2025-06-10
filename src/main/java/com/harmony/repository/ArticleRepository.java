package com.harmony.repository;

import com.harmony.entity.Article;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
  @Query("SELECT a FROM Article a WHERE a.author.userId IN (" +
      "SELECT f.friend.userId FROM Friendship f WHERE f.user.userId = :userId)"
      + "ORDER BY a.createdAt DESC")
  List<Article> findArticlesByFriendsOfUserOrderByCreatedAtDesc(@Param("userId") Long userId);

  @Query("SELECT a FROM Article a WHERE a.author.userId = :authorId ORDER BY a.createdAt DESC")
  List<Article> findArticlesByUserIdOrderByCreatedAtDesc(@Param("authorId") Long authorId);

  @Query("SELECT a FROM Article a WHERE a.author.userId = :authorId")
  Optional<Article> findArticleByAuthorId(@Param("authorId") Long authorId);

  @Query("SELECT a FROM Article a WHERE a.articleId = :articleId")
  Optional<Article> findArticleByArticleId(@Param("articleId") Long articleId);
}
