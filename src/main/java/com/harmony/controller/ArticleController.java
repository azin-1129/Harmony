package com.harmony.controller;

import com.harmony.dto.form.ArticleForm;
import com.harmony.dto.request.WriteArticleRequestDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value="/article")
@RequiredArgsConstructor
@RestController
public class ArticleController {
  private final ArticleService articleService;
  private final Long givenUserId=1L;

  // 게시물 작성(이미지+텍스트)
  @PostMapping("/write")
  @Operation(summary="이미지+텍스트 게시물 작성", description = "게시물 작성 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode="201", description = "게시물 작성에 성공했습니다.")
  })
  public ResponseEntity<Object> writeArticle(@RequestBody WriteArticleRequestDto writeArticleRequestDto){
    return SuccessResponse.createSuccess(
        SuccessCode.WRITE_ARTICLE_SUCCESS,
        articleService.writeArticle(givenUserId, writeArticleRequestDto)
    );
  }

  // TODO: 게시물 작성(비디오)

  // 게시물 조회

  // 조회 1. 팔로잉 피드 최신순 정렬
  @GetMapping("/following")
  @Operation(summary="팔로잉 피드 최신순 정렬", description = "팔로잉 피드 최신순 정렬 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode="200", description = "팔로잉 피드 최신순 정렬에 성공했습니다.")
  })
  public ResponseEntity<Object> selectFollowingUserArticles(){
    return SuccessResponse.createSuccess(
        SuccessCode.FOLLOWING_USER_ARTICLES_READ_SUCCESS,
        articleService.selectFollowingUserArticles(givenUserId)
    );
  }

  // 조회 2. 특정 유저가 작성한 피드 최신순 정렬
  @GetMapping("/{userId}")
  @Operation(summary="특정 유저가 작성한 피드 최신순 정렬", description = "특정 유저가 작성한 피드 최신순 정렬 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode="200", description = "특정 유저의 피드 최신순 정렬에 성공했습니다."),
      @ApiResponse(responseCode = "404", description="이미 탈퇴한 회원입니다.")
  })
  public ResponseEntity<Object> selectUserArticles(@PathVariable Long userId){
    return SuccessResponse.createSuccess(
        SuccessCode.USER_ARTICLES_READ_SUCCESS,
        articleService.selectUserArticles(userId)
    );
  }

  // TODO: 게시물 조회(비디오)

  // 게시물 수정
  @PostMapping("/update/{articleId}")
  @Operation(summary="게시물 수정", description = "게시물 수정 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode="200", description = "게시물 수정에 성공했습니다."),
      @ApiResponse(responseCode = "404", description="존재하지 않는 게시물입니다.")
  })
  public ResponseEntity<Object> updateArticle(@PathVariable Long articleId, @RequestBody ArticleForm articleForm){
    articleService.updateArticle(articleId, articleForm);

    return SuccessResponse.createSuccess(
        SuccessCode.ARTICLE_UPDATE_SUCCESS
    );
  }

  // TODO: 게시물 수정(비디오)

  // 게시물 삭제
  @DeleteMapping("/{articleId}")
  @Operation(summary="게시물 삭제", description = "게시물 삭제 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode="200", description = "게시물 삭제에 성공했습니다."),
      @ApiResponse(responseCode = "404", description="존재하지 않는 게시물입니다.")
  })
  public ResponseEntity<Object> deleteArticle(@PathVariable Long articleId){
    articleService.deleteArticle(articleId);

    return SuccessResponse.createSuccess(
        SuccessCode.ARTICLE_DELETE_SUCCESS
    );
  }
}
