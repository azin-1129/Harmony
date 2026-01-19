package com.harmony.service;

import com.harmony.dto.request.WriteCommentRequestDto;
import com.harmony.dto.response.SelectCommentResponseDto;
import com.harmony.dto.response.WriteCommentResponseDto;
import com.harmony.entity.Article;
import com.harmony.entity.Comment;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ArticleRepository;
import com.harmony.repository.CommentRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {
  private final CommentRepository commentRepository;
  private final ArticleRepository articleRepository;
  private final UserService userService;
  private final AwsS3Service awsS3Service;

  // 댓글 작성
  public WriteCommentResponseDto writeComment(Long givenUserId, WriteCommentRequestDto writeCommentRequestDto){
    Article article=articleRepository.findArticleByArticleId(writeCommentRequestDto.getArticleId())
        .orElseThrow(()->new EntityNotFoundException(
            ErrorCode.ARTICLE_NOT_FOUND
        ));
    article.updateArticleCommentCount(true);

    User writer=userService.getUserById(givenUserId);

    Comment comment=Comment.builder()
        .article(article)
        .writer(writer)
        .commentText(writeCommentRequestDto.getCommentText())
        .build();

    commentRepository.save(comment);

    WriteCommentResponseDto writeCommentResponseDto=WriteCommentResponseDto.builder()
        .commentId(comment.getCommentId())
        .commentText(writeCommentRequestDto.getCommentText())
        .createdAt(comment.getCreatedAt())
        .build();

    return writeCommentResponseDto;
  }

  // TODO: 댓글 GIF 작성

  // 댓글 불러오기
  public List<SelectCommentResponseDto> selectComments(Long articleId){
    List<Comment> comments=commentRepository.findCommentsByArticleIdOrderByCreatedAtDesc(articleId);
    List<SelectCommentResponseDto> selectCommentResponseDtos=new ArrayList<>();
    for(Comment comment:comments){
      User writer=comment.getWriter();
      String profileImageUrl = null;
      String profileImageName=writer.getProfileImageName();
      if(profileImageName!=null){
        profileImageUrl= awsS3Service.getUserProfileImageUrl(profileImageName);
      }
      SelectCommentResponseDto selectCommentResponseDto=SelectCommentResponseDto.builder()
          .commentId(comment.getCommentId())
          .writerIdentifier(writer.getUserIdentifier())
          .writerProfileImageUrl(profileImageUrl)
          .nickname(writer.getNickname())
          .commentText(comment.getCommentText())
          .createdAt(comment.getCreatedAt())
          .build();

      selectCommentResponseDtos.add(selectCommentResponseDto);
    }

    return selectCommentResponseDtos;
  }

  // 댓글 삭제
  public void deleteComment(Long commentId){
    Comment comment=commentRepository.findById(commentId).orElseThrow(()->new EntityNotFoundException(
        ErrorCode.COMMENT_NOT_FOUND
    ));

    commentRepository.delete(comment);
  }
}
