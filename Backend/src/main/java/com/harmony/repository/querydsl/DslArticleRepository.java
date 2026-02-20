package com.harmony.repository.querydsl;

import com.harmony.dto.response.SelectFollowingUserArticlesResponseDto;
import com.harmony.dto.response.SlicedResponseDto;
import org.springframework.data.domain.Pageable;

public interface DslArticleRepository {
  SlicedResponseDto<SelectFollowingUserArticlesResponseDto> findArticleSliceByFriendsOfUserOrderByCreatedAtDesc(Long userId, Long articleId, Pageable pageable);
}
