package org.sopt.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  // OAuth 유저는 비밀번호가 없으므로 nullable
  // 로그인 시 password == null이면 BCrypt 비교 대신 dummyHash를 사용해 항상 실패 처리
  @Column(nullable = true)
  private String password;

  @Column(nullable = false, unique = true)
  private String email;

  // 기존 row 마이그레이션을 위해 DB 기본값 'LOCAL'로 설정
  // ddl-auto: update 시 새 컬럼이 추가되며 기존 row는 자동으로 LOCAL로 채워짐
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'LOCAL'")
  private AuthProvider provider = AuthProvider.LOCAL;

  protected User() {}

  public User(String nickname, String email, String password) {
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.provider = AuthProvider.LOCAL;
  }

  // OAuth 유저 생성 — 비밀번호 없음, provider 명시
  public static User ofOAuth(String nickname, String email, AuthProvider provider) {
    User user = new User();
    user.nickname = nickname;
    user.email = email;
    user.password = null;
    user.provider = provider;
    return user;
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

  public String getPassword() {
    return this.password;
  }
}