package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@Entity
@Table(name = "comments")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTime {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="comment_id", nullable=false)
  private Long commentId;

  @ManyToOne
  @JoinColumn(name="article_id")
  private Article article;

  @ManyToOne
  @JoinColumn(name="user_id")
  private User writer;

  @Column(name="comment_text", nullable=true, length=255)
  private String commentText;

  @Column(name="comment_media_url", nullable=true, length=255)
  private String commentMediaUrl;
}
