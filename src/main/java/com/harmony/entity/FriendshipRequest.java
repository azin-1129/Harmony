package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "friendship_requests")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendshipRequest {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="friendship_request_id", nullable = false)
  private Long friendshipRequestId;

  @ManyToOne(fetch= FetchType.LAZY)
  @JoinColumn(name="from_user_id")
  private User friendshipRequestSender;

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="to_user_id")
  private User friendshipRequestReceiver;

  @Enumerated(EnumType.STRING)
  @Column(name="friendship_req_status", nullable = false)
  private FriendshipRequestStatus friendshipRequestStatus;

  public void setFriendshipRequestInfo(User friendshipRequestSender, User friendshipRequestReceiver) {
    this.friendshipRequestSender = friendshipRequestSender;
    this.friendshipRequestReceiver = friendshipRequestReceiver;
    friendshipRequestSender.getSentFriendshipRequests().add(this);
    friendshipRequestReceiver.getReceivedFriendshipRequests().add(this);
  }

  public void setFriendshipRequestStatus(FriendshipRequestStatus friendshipRequestStatus) {
    this.friendshipRequestStatus = friendshipRequestStatus;
  }

  @Override
  public String toString(){
    return "FriendshipRequest [friendshipRequestId=" + friendshipRequestId
        + ", friendshipRequestSender=" + friendshipRequestSender.getUserIdentifier()
        + ", friendshipRequestReceiver=" + friendshipRequestReceiver.getUserIdentifier()
        + ", friendshipRequestStatus=" + friendshipRequestStatus + "]";
  }
}
