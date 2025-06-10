package com.harmony.dto.response;

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
public class SelectFollowingUserArticlesResponseDto {
  private String authorIdentifier;
  private String authorProfileImageName;
  private String articleMediaUrl;
  private String articleText;
  private Integer articleCommentCount;
  private Integer articleLikeCount;
  private String articleThumbnailUrl;
}
