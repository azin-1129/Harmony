package com.harmony.controller;

import com.harmony.dto.request.WriteCommentRequestDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/comment")
@RequiredArgsConstructor
@RestController
public class CommentController {
  private final CommentService commentService;
  private final Long givenUserId=1L;

  // 댓글 작성
  @PostMapping
  public ResponseEntity<Object> writeComment(@RequestBody WriteCommentRequestDto writeCommentRequestDto){
    return SuccessResponse.createSuccess(
        SuccessCode.COMMENT_CREATE_SUCCESS,
        commentService.writeComment(givenUserId, writeCommentRequestDto)
    );
  }

  // 댓글 조회? (게시물에 딸려서 조회됨) - 시간순 정렬 필요
  @GetMapping("/{articleId}")
  public ResponseEntity<Object> selectComments(@PathVariable Long articleId){
    return SuccessResponse.createSuccess(
        SuccessCode.COMMENT_READ_SUCCESS,
        commentService.selectComments(articleId)
    );
  }
  // 댓글 수정(instagram엔 없음)

  // 댓글 삭제
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Object> deleteComment(@PathVariable Long commentId){
    commentService.deleteComment(commentId);

    return SuccessResponse.createSuccess(
        SuccessCode.COMMENT_DELETE_SUCCESS
    );
  }
}
