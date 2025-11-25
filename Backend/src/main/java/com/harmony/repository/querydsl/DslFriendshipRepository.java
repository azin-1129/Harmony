package com.harmony.repository.querydsl;

public interface DslFriendshipRepository {
  Boolean existsFriendship(Long userId, Long friendId);
}
