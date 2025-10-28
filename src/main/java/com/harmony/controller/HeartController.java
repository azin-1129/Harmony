package com.harmony.controller;

import com.harmony.global.annotation.CurrentUserId;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.HeartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value="/heart")
@RequiredArgsConstructor
@RestController
public class HeartController {
  private final HeartService heartService;

  // 게시물 좋아요+취소
  @PostMapping("/{articleId}")
  public ResponseEntity<Object> toggleArticleHeart(@CurrentUserId Long userId, @PathVariable Long articleId){

    return SuccessResponse.createSuccess(
        SuccessCode.ARTICLE_HEART_SUCCESS,
        heartService.toggleArticleHeart(articleId, userId)
    );
  }

  // 내 좋아요 목록
  @GetMapping
  public ResponseEntity<Object> selectMyArticleHearts(@CurrentUserId Long userId){
    return SuccessResponse.createSuccess(
        SuccessCode.MY_ARTICLE_HEART_READ_SUCCESS,
        heartService.selectMyArticleHearts(userId)
    );
  }
}
