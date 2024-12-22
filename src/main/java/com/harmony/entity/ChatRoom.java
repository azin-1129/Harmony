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

  @Column(name = "chatroom_count", nullable = false, columnDefinition = "smallint(1500)")
  private Integer chatRoomCount;

  @Column(name = "chatroom_count_max", nullable = false, columnDefinition = "smallint(1500)")
  private Integer chatRoomCountMax;

  @Column(name="chatroom_code", nullable = true, length=20)
  private String chatRoomCode;

  @Enumerated(EnumType.STRING)
  @Column(name="chatroom_type", nullable = false)
  private ChatRoomType chatRoomType;
}