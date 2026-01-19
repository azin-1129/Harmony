package com.harmony.entity;

import com.harmony.dto.form.ArticleForm;
import com.harmony.global.BaseTime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
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

  @Column(name="article_media_name", nullable=false, length=255)
  private String articleMediaName;

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

  @OneToMany(fetch= FetchType.LAZY, mappedBy="article", cascade= CascadeType.ALL, orphanRemoval=true)
  private List<Comment> comments;

  @Column(name="is_deleted", nullable=false)
  private Boolean isDeleted;

  // update
  public void updateArticleMediaName(String newArticleMediaName){
    this.articleMediaName = newArticleMediaName;
  }

  public void updateArticleText(String newArticleText){
    this.articleText = newArticleText;
  }

  public void updateIsDeleted(Boolean isDeleted){
    this.isDeleted = isDeleted;
  }

  public void updateArticleCommentCount(boolean commentStatus){
    if(commentStatus){
      this.articleCommentCount++;
    }else{
      this.articleCommentCount--;
    }
  }

  public void updateArticleLikeCount(boolean heartStatus){
    if(heartStatus){
      this.articleLikeCount++;
    }else{
      this.articleLikeCount--;
    }
  }
}
