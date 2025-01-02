package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "friends")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="friend_id", nullable = false)
  private Long friendId;

  @ManyToOne
  @JoinColumn(name="from_user_id")
  private User fromUser;

  @ManyToOne
  @JoinColumn(name="to_user_id")
  private User toUser;

  @Column(name="is_friend", nullable=false)
  Boolean isFriend;

  @Enumerated(EnumType.STRING)
  @Column(name="friend_type", nullable = false)
  FriendType friendType;
}
