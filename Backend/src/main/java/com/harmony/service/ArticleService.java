package com.harmony.service;

import com.harmony.dto.form.ArticleForm;
import com.harmony.dto.request.WriteArticleRequestDto;
import com.harmony.dto.response.SelectFollowingUserArticlesResponseDto;
import com.harmony.dto.response.SelectAuthorArticlesResponseDto;
import com.harmony.entity.Article;
import com.harmony.entity.ArticleMediaType;
import com.harmony.entity.User;
import com.harmony.exception.UserAlreadyWithdrawException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ArticleRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final UserService userService;

  // 게시물 작성
  public Long writeArticle(Long givenUserId, WriteArticleRequestDto writeArticleRequestDto){
    User author = userService.getUserById(
        givenUserId);

    Article article=Article.builder()
        .author(author)
        .articleMediaUrl(writeArticleRequestDto.getArticleMediaUrl())
        .articleMediaType(ArticleMediaType.IMAGE)
        .articleText(writeArticleRequestDto.getArticleText())
        .articleCommentCount(0)
        .isCommentDisabled(false)
        .articleLikeCount(0)
        .build();

    articleRepository.save(article);

    return article.getArticleId();
  }

  // 게시물 조회

  // 조회 1. 팔로잉 피드 최신순 정렬
  public List<SelectFollowingUserArticlesResponseDto> selectFollowingUserArticles(Long givenUserId){
    List<Article> articles=articleRepository.findArticlesByFriendsOfUserOrderByCreatedAtDesc(givenUserId);

    List<SelectFollowingUserArticlesResponseDto> selectFollowingUserArticlesResponseDtos=new ArrayList<>();

    for(Article article:articles){
      SelectFollowingUserArticlesResponseDto selectFollowingUserArticlesResponseDto=SelectFollowingUserArticlesResponseDto.builder()
          .authorIdentifier(article.getAuthor().getUserIdentifier())
          .authorProfileImageName(article.getAuthor().getProfileImageName())
          .articleMediaUrl(article.getArticleMediaUrl())
          .articleText(article.getArticleText())
          .articleCommentCount(article.getArticleCommentCount())
          .articleLikeCount(article.getArticleLikeCount())
          .articleThumbnailUrl(article.getArticleThumbnailUrl())
          .createdAt(article.getCreatedAt())
          .build();

      selectFollowingUserArticlesResponseDtos.add(selectFollowingUserArticlesResponseDto);
    }

    return selectFollowingUserArticlesResponseDtos;
  }

  // 조회 2. 특정 유저가 작성한 피드 최신순 정렬
  public List<SelectAuthorArticlesResponseDto> selectUserArticles(Long authorId){
    User author=userService.getUserById(authorId);
    if(author.getWithdraw()){
      throw new UserAlreadyWithdrawException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    List<Article> articles=articleRepository.findArticlesByUserIdOrderByCreatedAtDesc(authorId);

    List<SelectAuthorArticlesResponseDto> selectUserArticlesResponseDtos=new ArrayList<>();

    for(Article article:articles){
      SelectAuthorArticlesResponseDto selectUserArticlesResponseDto= SelectAuthorArticlesResponseDto.builder()
          .authorIdentifier(article.getAuthor().getUserIdentifier())
          .authorProfileImageName(article.getAuthor().getProfileImageName())
          .articleMediaUrl(article.getArticleMediaUrl())
          .articleText(article.getArticleText())
          .articleCommentCount(article.getArticleCommentCount())
          .articleLikeCount(article.getArticleLikeCount())
          .articleThumbnailUrl(article.getArticleThumbnailUrl())
          .createdAt(article.getCreatedAt())
          .build();

      selectUserArticlesResponseDtos.add(selectUserArticlesResponseDto);
    }

    return selectUserArticlesResponseDtos;
  }

  // 게시물 수정
  public void updateArticle(Long articleId, ArticleForm articleForm){
    Article article=articleRepository.findArticleByArticleId(articleId).orElseThrow(
        () -> new EntityNotFoundException(ErrorCode.ARTICLE_NOT_FOUND)
    );
    article.updateContents(articleForm);
  }

  // 게시물 삭제
  public void deleteArticle(Long articleId){
    Article article=articleRepository.findArticleByArticleId(articleId).orElseThrow(
        () -> new EntityNotFoundException(ErrorCode.ARTICLE_NOT_FOUND)
    );
    articleRepository.delete(article);
  }
}
