package com.harmony.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelectCommentResponseDto {
  private Long writerId;
  private String profileImageName;
  private String nickname;
  private String commentText;
  private LocalDateTime createdAt;
}
