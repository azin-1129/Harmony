package com.harmony.service;

import com.harmony.dto.response.SelectMyArticleHeartsResponseDto;
import com.harmony.entity.Article;
import com.harmony.entity.Heart;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ArticleRepository;
import com.harmony.repository.HeartRepository;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class HeartService {
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final HeartRepository heartRepository;

  // 게시물 좋아요+취소
  public boolean toggleArticleHeart(Long articleId, Long userId){
    // 유저 조회
    User user=userRepository.findByUserId(userId).orElseThrow(
        () -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND)
    );

    // 게시물 조회
    Article article=articleRepository.findArticleByArticleId(articleId).orElseThrow(
        () -> new EntityNotFoundException(ErrorCode.ARTICLE_NOT_FOUND)
    );

    // 하트(좋아요)가 이미 존재한다면, 제거
    Optional<Heart> originHeart=heartRepository.findByUserIdAndArticleId(userId, articleId);
    if(originHeart.isPresent()){
      originHeart.get().removeHeartInfo();
      article.updateArticleLikeCount(false);
      return false;
    }

    // 하트 빌드
    Heart newHeart=Heart.builder()
        .user(user)
        .article(article)
        .build();

    article.updateArticleLikeCount(true);

    // 연관관계 설정
    newHeart.setHeartInfo();

    // 저장
    heartRepository.save(newHeart);

    return true;
  }

  // 내 좋아요 목록
  public List<SelectMyArticleHeartsResponseDto> selectMyArticleHearts(Long userId){
    User user=userRepository.findByUserId(userId).orElseThrow(
        () -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND)
    );

    List<Heart> hearts=user.getHearts();
    List<SelectMyArticleHeartsResponseDto> selectMyArticleHeartsResponseDtos=new ArrayList<>();
    for(Heart heart:hearts){
      Article article=heart.getArticle();
      SelectMyArticleHeartsResponseDto selectMyArticleHeartsResponseDto=SelectMyArticleHeartsResponseDto.builder()
          .articleId(article.getArticleId())
          .articleThumbnailUrl(article.getArticleThumbnailUrl())
          .build();
      selectMyArticleHeartsResponseDtos.add(selectMyArticleHeartsResponseDto);
    }

    return selectMyArticleHeartsResponseDtos;
  }
}
