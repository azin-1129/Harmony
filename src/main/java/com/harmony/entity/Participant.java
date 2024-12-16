package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "participants")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

  @EmbeddedId
  private ParticipantId participantsId;

  @MapsId("chatroomId")
  @ManyToOne
  @JoinColumn(name="chatroom_id")
  private ChatRoom chatRoom;

  @MapsId("userNo")
  @ManyToOne
  @JoinColumn(name="user_no")
  private User user;
}
