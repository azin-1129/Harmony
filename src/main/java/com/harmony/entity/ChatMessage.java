package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
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
@Table(name = "chatmessages")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTime {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="chat_message_id", nullable = false)
  private Long chatMessageId;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name="user_id"),
      @JoinColumn(name="chatroom_id"),
  })
  private Participant participant;

  @Enumerated(EnumType.STRING)
  @Column(name="chat_message_type", nullable = false)
  private ChatMessageType chatMessageType;

  @Column(name="chat_message", nullable = false, length=500)
  private String chatMessage;

  @Column(name="is_read", nullable=false)
  private Boolean isRead;
}