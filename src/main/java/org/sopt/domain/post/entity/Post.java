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
import jakarta.persistence.Version;
import org.sopt.domain.user.entity.User;
import org.sopt.global.entity.BaseTimeEntity;

@Entity
public class Post extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BoardType boardType;

  // 낙관적 락의 핵심: 커밋 시 UPDATE post SET version = ? WHERE id = ? AND version = ?
  // 기댓값과 실제값이 다르면 OptimisticLockingFailureException 발생
  @Version
  @Column(columnDefinition = "BIGINT DEFAULT 0")
  private Long version;

  // 좋아요 수 비정규화 — 별도 COUNT 쿼리 없이 Post 조회만으로 응답 가능
  // likeCount를 변경함으로써 version 체크가 트리거되어 동시성을 제어한다
  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private long likeCount = 0;

  protected Post() {}

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
  public long getLikeCount() { return likeCount; }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public void incrementLikeCount() {
    this.likeCount++;
  }

  public void decrementLikeCount() {
    // 좋아요 존재 여부를 먼저 검증한 후 호출하지만, 방어적으로 음수 방지
    if (this.likeCount > 0) this.likeCount--;
  }
}
