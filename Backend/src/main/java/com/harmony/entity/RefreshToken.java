package com.harmony.entity;

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
@Builder
@Entity
@Table(name="refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class RefreshToken {
  @Id
  @Column(name="user_email")
  private String userEmail;
  @Column(name="refresh_token")
  private String refreshToken;

  public RefreshToken updateRefreshToken(String refreshToken){
    this.refreshToken = refreshToken;
    return this;
  }
}
