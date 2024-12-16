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
@Table(name = "users")
public class User extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_no", nullable = false)
  private Long userNo;

  @Column(name = "user_id", nullable = false, length=10)
  private String userId;

  @Column(name = "password", nullable = false, length=20)
  private String password;

  @Column(name = "profile_image_name", nullable = false, length=255)
  private String profileImageName;

  @Column(name = "nickname", nullable = false, length=20)
  private String nickname;

  @Column(name = "withdraw", nullable = false)
  private Boolean withdraw;
}
