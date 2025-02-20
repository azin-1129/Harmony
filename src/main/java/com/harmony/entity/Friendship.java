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

@Getter
@Builder
@Entity
@Table(name = "friendships")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="friendship_id", nullable = false)
  private Long friendshipId;

  @ManyToOne
  @JoinColumn(name="friend_id")
  private User friend;

  @ManyToOne
  @JoinColumn(name="user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name="friend_type", nullable = false)
  FriendType friendType;

  public void setUsers(User user, User friend) {
    this.user = user;
    this.friend = friend;
    user.getFriendships().add(this);
  }

  @Override
  public String toString(){
    return "Friendship [friendshipId=" + friendshipId
        + ", friend=" + friend.getUserIdentifier()
        + ", user=" + user.getUserIdentifier()
        + ", friendType=" + friendType + "]";
  }
}
