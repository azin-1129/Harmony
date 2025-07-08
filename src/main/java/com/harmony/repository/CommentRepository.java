package com.harmony.repository;

import com.harmony.entity.Comment;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("SELECT c FROM Comment c WHERE c.writer.userId=:userId")
  List<Comment> findCommentsByUserId(@Param("userId") Long userId);

  @Transactional
  @Modifying(clearAutomatically=true)
  @Query("DELETE FROM Comment c WHERE c.commentId in :ids")
  void deleteCommentsBulk(@Param("ids") List<Long> ids);
}
