package com.harmony.dto.response;

import java.util.List;
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
public class SlicedResponseDto<T> {
  private List<T> content;
  private Long lastContentId;
  private boolean hasNext;

  public SlicedResponseDto(Long lastContentId, List<T> content, boolean hasNext) {
    this.content=content;
    this.hasNext = hasNext;
    this.lastContentId = lastContentId;
  }
}
