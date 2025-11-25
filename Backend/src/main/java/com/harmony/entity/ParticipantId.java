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
public class ParticipantId implements Serializable {
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "chatroom_id")
  private Long chatroomId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ParticipantId participantsId1 = (ParticipantId) o;

    return (chatroomId == participantsId1.chatroomId) && (userId == participantsId1.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chatroomId, userId);
  }
}