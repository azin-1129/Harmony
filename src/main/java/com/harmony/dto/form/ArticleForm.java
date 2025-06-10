package com.harmony.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleForm {
  @Schema(description="수정할 게시물 image", defaultValue = "potato.png")
  private String articleMediaUrl;
  @Schema(description="수정할 게시물 text", defaultValue = "생각해 보니 감자가 낫네요.")
  private String articleText;
}
