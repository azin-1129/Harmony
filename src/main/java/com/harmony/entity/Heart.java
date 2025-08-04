package com.harmony.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "hearts")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Heart {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name="heart_id", nullable=false)
  private Long heartId;

  @ManyToOne
  @JoinColumn(name="user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name="article_id")
  private Article article;

  @Column(name="heart_status", nullable=false)
  private Boolean heartStatus;

  // update
  public boolean updateHeartStatus(){
    this.heartStatus = !this.heartStatus;
    return this.heartStatus;
  }
}
