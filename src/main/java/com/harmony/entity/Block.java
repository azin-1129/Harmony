package com.harmony.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@Entity
@Table(name = "blocks")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {
  @EmbeddedId
  private BlockId blockId;

  @MapsId("blockingUserId")
  @ManyToOne
  @JoinColumn(name="blocking_user_id")
  private User blockingUser;

  @MapsId("blockedUserId")
  @ManyToOne
  @JoinColumn(name="blocked_user_id")
  private User blockedUser;
}
