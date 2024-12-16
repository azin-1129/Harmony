package com.harmony.entity;

import com.harmony.global.BaseTime;
import jakarta.persistence.Column;
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
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatmessages")
public class ChatMessage extends BaseTime {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="message_id", nullable = false)
  private Long messageId;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name="user_no"),
      @JoinColumn(name="chatroom_id"),
  })
  private Participant participant;

  @Enumerated(EnumType.STRING)
  @Column(name="message_type", nullable = false)
  private ChatMessageType messageType;

  @Column(name="message", nullable = false, length=500)
  private String message;
}