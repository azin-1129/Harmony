package com.harmony.dto.request;

import com.harmony.entity.ArticleMediaType;
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
public class WriteArticleRequestDto {
  @Schema(description="업로드할 게시물 image", defaultValue="gold_kiwi.png")
  private String articleMediaUrl;
  @Schema(description="업로드할 게시물 text", defaultValue="이건 키위라는 거에요")
  private String articleText;
}
