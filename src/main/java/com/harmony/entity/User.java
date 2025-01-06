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
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_email", nullable = false, length=255)
  private String email;

  @Column(name="user_identifier", nullable=false, length=20)
  private String userIdentifier;

  @Column(name = "password", nullable = false, length=20)
  private String password;

  @Column(name = "profile_image_name", nullable = false, length=255)
  private String profileImageName;

  @Column(name = "nickname", nullable = false, length=20)
  private String nickname;

  @Column(name = "withdraw", nullable = false)
  private Boolean withdraw;

  @Enumerated(EnumType.STRING)
  @Column(name="role", nullable = false)
  private Role role;

  // update
  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }

  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
  }

  public void updateProfileImage(String newProfileImageName) {
    this.profileImageName = newProfileImageName;
  }
}
