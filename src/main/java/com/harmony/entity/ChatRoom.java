package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "chatrooms")
public class ChatRoom extends BaseTime {

  @Id
  @GeneratedValue(strategy=GenerationType.UUID)
  @Column(name = "chatroom_id")
  private Long chatRoomId;

  @Column(name = "chatroom_name", nullable = false)
  private String chatRoomName;

  @Column(name = "chatroom_count", nullable = false)
  private Boolean chatRoomCount;
}
