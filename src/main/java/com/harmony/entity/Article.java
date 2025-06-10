package com.harmony.entity;

import com.harmony.dto.form.ArticleForm;
import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "articles")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseTime {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="article_id", nullable=false)
  private Long articleId;

  @ManyToOne
  @JoinColumn(name="user_id")
  private User author;

  @Column(name="article_media_url", nullable=false, length=255)
  private String articleMediaUrl;

  @Enumerated(EnumType.STRING)
  @Column(name="article_media_type", nullable=false)
  private ArticleMediaType articleMediaType;

  @Column(name="article_text", nullable=true, length=255)
  private String articleText;

  @Column(name="article_comment_count", nullable=false, columnDefinition="smallint")
  private Integer articleCommentCount;

  @Column(name="is_comment_disabled", nullable=false)
  private Boolean isCommentDisabled;

  @Column(name="article_like_count", nullable=false, columnDefinition="smallint")
  private Integer articleLikeCount;

  @Column(name="article_thumbnail_url", nullable=true, length=255)
  private String articleThumbnailUrl;

  // update
  public void updateContents(ArticleForm articleForm){
    this.articleMediaUrl = articleForm.getArticleMediaUrl();
    this.articleText = articleForm.getArticleText();
  }
}
