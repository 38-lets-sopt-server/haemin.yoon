package org.sopt.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.sopt.domain.user.entity.User;
import org.sopt.global.entity.BaseTimeEntity;

@Entity
public class Post extends BaseTimeEntity {

  @Id // 앞에서 배운 PK
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;     // 목록, 상세, 글쓰기 화면 — 제목

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;   // 목록(미리보기), 상세(전체) 화면 — 내용

  @ManyToOne(fetch = FetchType.LAZY)  // User : Post = 1 : N
  @JoinColumn(name = "user_id")       // post 테이블에 user_id FK 컬럼 추가
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BoardType boardType; // 필드 추가

  protected Post() {}  // JPA 기본 생성자

  public Post(Long id, String title, String content, User user, BoardType boardType) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.user = user;
    this.boardType = boardType;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getContent() { return content; }
  public BoardType getBoardType() { return boardType; }
  public User getUser() { return user; }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }
}
