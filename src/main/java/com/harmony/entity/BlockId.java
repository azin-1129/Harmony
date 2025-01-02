package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockId implements Serializable {
  @Column(name = "blocking_user_id")
  private Long blockingUserId;

  @Column(name = "blocked_user_id")
  private Long blockedUserId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BlockId blockId1 = (BlockId) o;

    return (blockingUserId == blockId1.blockingUserId) && (blockedUserId == blockId1.blockedUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockingUserId, blockedUserId);
  }
}
