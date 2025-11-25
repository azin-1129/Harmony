package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "chatrooms")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chatroom_id", nullable = false)
  private Long chatRoomId;

  @Column(name = "chatroom_name", nullable = false, length=30)
  private String chatRoomName;

  @Column(name = "chatroom_count", nullable = false, columnDefinition = "smallint")
  private Integer chatRoomCount;

  @Column(name = "chatroom_count_max", nullable = false, columnDefinition = "smallint")
  private Integer chatRoomCountMax;

  @Enumerated(EnumType.STRING)
  @Column(name="chatroom_type", nullable = false)
  private ChatRoomType chatRoomType;

  // update
  public void updateChatRoomName(String newChatRoomName) {
    this.chatRoomName = newChatRoomName;
  }

  public void updateChatRoomCountPositive() {
    this.chatRoomCount+=1;
  }

  public void updateChatRoomCountNegative() {
    this.chatRoomCount-=1;
  }
}