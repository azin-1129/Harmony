package com.harmony.repository.querydsl;

import com.harmony.entity.FriendshipRequestStatus;

public interface DslFriendshipRequestRepository {
  Boolean existsFriendshipRequest(Long blockingId, Long blockedId, FriendshipRequestStatus status);
}
