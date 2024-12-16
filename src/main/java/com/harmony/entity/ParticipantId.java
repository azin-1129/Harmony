package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantId implements Serializable {
  // TODO: Serializable 구현이 잘 되었는가?
  @Column(name = "chatroom_id")
  private UUID chatroomId;

  @Column(name = "user_no")
  private Long userNo;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ParticipantId participantsId1 = (ParticipantId) o;

    return (chatroomId == participantsId1.chatroomId) && (userNo == participantsId1.userNo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chatroomId, userNo);
  }
}