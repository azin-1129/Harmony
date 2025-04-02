package com.harmony.repository.querydsl.impl;

import static com.harmony.entity.QFriendshipRequest.friendshipRequest;

import com.harmony.entity.FriendshipRequestStatus;
import com.harmony.repository.querydsl.DslFriendshipRequestRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DslFriendshipRequestRepositoryImpl implements DslFriendshipRequestRepository {
  private final JPAQueryFactory qf;

  @Override
  public Boolean existsFriendshipRequest(Long blockingId, Long blockedId, FriendshipRequestStatus status) {
    Integer one= qf
        .selectOne()
        .from(friendshipRequest)
        .where(friendshipRequest.friendshipRequestSender.userId.eq(blockingId),
            friendshipRequest.friendshipRequestReceiver.userId.eq(blockedId),
            friendshipRequest.friendshipRequestStatus.eq(status))
        .fetchFirst(); // 내부적으로 limit(1).fetchOne()

    return one != null;
  }
}
