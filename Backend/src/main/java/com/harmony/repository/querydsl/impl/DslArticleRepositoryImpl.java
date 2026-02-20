package com.harmony.repository.querydsl.impl;

import com.harmony.dto.response.SelectFollowingUserArticlesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import com.harmony.entity.Article;
import static com.harmony.entity.QArticle.article;
import static com.harmony.entity.QFriendship.friendship;
import static com.harmony.entity.QUser.user;

import com.harmony.repository.querydsl.DslArticleRepository;
import com.harmony.service.AwsS3Service;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DslArticleRepositoryImpl implements DslArticleRepository {
  private final AwsS3Service awsS3Service;
  private final JPAQueryFactory qf;

  @Override
  public SlicedResponseDto<SelectFollowingUserArticlesResponseDto> findArticleSliceByFriendsOfUserOrderByCreatedAtDesc(Long userId, Long articleId, Pageable pageable){
    List<Article> articles=qf
        .selectDistinct(article)
        .from(article)
        .join(article.author, user).fetchJoin()
        .where(
            article.author.userId.in(
              JPAExpressions
                  .select(friendship.friend.userId)
                  .from(friendship)
                  .where(
                      friendship.user.userId.eq(userId),
                      friendship.friend.withdraw.isFalse()
                  )
              ),
          articleIdLt(articleId)
        )
        .orderBy(article.createdAt.desc()) // 이거랑 pageable의 sort랑 먼 차이임?
        .limit(pageable.getPageSize()+1)
        .fetch();

    // Dto 가공
    List<SelectFollowingUserArticlesResponseDto> selectFollowingUserArticlesResponseDtos=new ArrayList<>();

    for(Article article:articles){
      String articleImageUrl=awsS3Service.getArticleImageUrl(article.getArticleMediaName());
      String authorProfileImageName=article.getAuthor().getProfileImageName();
      SelectFollowingUserArticlesResponseDto selectFollowingUserArticlesResponseDto=SelectFollowingUserArticlesResponseDto.builder()
          .articleId(article.getArticleId())
          .authorIdentifier(article.getAuthor().getUserIdentifier())
          .authorProfileImageUrl(authorProfileImageName==null?null:awsS3Service.getAuthorProfileImageUrl(authorProfileImageName))
          .articleMediaUrl(articleImageUrl)
          .articleText(article.getArticleText())
          .articleCommentCount(article.getArticleCommentCount())
          .articleLikeCount(article.getArticleLikeCount())
          .articleThumbnailUrl(articleImageUrl) // TODO: 썸네일 생성
          .createdAt(article.getCreatedAt())
          .build();

      selectFollowingUserArticlesResponseDtos.add(selectFollowingUserArticlesResponseDto);
    }

    // 마지막 article Id
    Long lastArticleId=
        articles.size()>pageable.getPageSize()-1 ?
            articles.get(pageable.getPageSize()-1).getArticleId() : null;

    // hasNext?
    boolean hasNext=false;
    if(articles.size()>pageable.getPageSize()){
      articles.subList(0, pageable.getPageSize());
      hasNext=true;
    }

    return new SlicedResponseDto<SelectFollowingUserArticlesResponseDto>(lastArticleId, selectFollowingUserArticlesResponseDtos, hasNext);
  }
  private BooleanExpression articleIdLt(Long articleId){
    return articleId!=null ? article.articleId.lt(articleId) : null;
  }
}
