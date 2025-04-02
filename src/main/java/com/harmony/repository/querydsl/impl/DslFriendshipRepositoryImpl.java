package com.harmony.repository.querydsl.impl;

import static com.harmony.entity.QFriendship.friendship;

import com.harmony.repository.querydsl.DslFriendshipRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DslFriendshipRepositoryImpl implements DslFriendshipRepository {
  private final JPAQueryFactory qf;

  @Override
  public Boolean existsFriendship(Long userId, Long friendId){
    Integer one= qf
        .selectOne()
        .from(friendship)
        .where(friendship.user.userId.eq(userId), friendship.friend.userId.eq(friendId))
        .fetchFirst(); // 내부적으로 limit(1).fetchOne()

    return one != null;
  }
}
