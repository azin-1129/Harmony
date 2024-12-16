package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
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
  @GeneratedValue(strategy=GenerationType.UUID)
  @Column(name = "chatroom_id", nullable = false)
  private UUID chatRoomId;

  @Column(name = "chatroom_name", nullable = false, length=30)
  private String chatRoomName;

  @Column(name = "chatroom_count", nullable = false, columnDefinition = "smallint")
  private Integer chatRoomCount;
}