package org.sopt.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sopt.global.entity.BaseTimeEntity;

@Entity
@Table(name = "users")  // "user"는 SQL 예약어라 테이블명을 변경해요
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false, unique = true)
  private String email;
  
  protected User() {}

  public User(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

  public Long getId() {
    return this.id;
  }

  public String getNickname() {
    return this.nickname;
  }

  public String getEmail() {
    return this.email;
  }
}