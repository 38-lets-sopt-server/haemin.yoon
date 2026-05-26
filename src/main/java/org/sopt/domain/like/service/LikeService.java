package org.sopt.domain.like.service;

import org.sopt.domain.like.entity.Like;
import org.sopt.domain.like.exception.LikeException;
import org.sopt.domain.like.exception.code.LikeErrorCode;
import org.sopt.domain.like.repository.LikeRepository;
import org.sopt.domain.post.entity.Post;
import org.sopt.domain.post.exception.PostException;
import org.sopt.domain.post.exception.code.PostErrorCode;
import org.sopt.domain.post.repository.PostRepository;
import org.sopt.domain.user.entity.User;
import org.sopt.domain.user.repository.UserRepository;
import org.sopt.global.exception.CustomException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

  private final LikeRepository likeRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public LikeService(
      LikeRepository likeRepository,
      PostRepository postRepository,
      UserRepository userRepository
  ) {
    this.likeRepository = likeRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  // @Retryable이 @Transactional 바깥을 감싸는 프록시로 동작한다.
  // 실행 순서: Retry 인터셉터 → 트랜잭션 시작 → 비즈니스 로직 → 트랜잭션 커밋(version 체크)
  // 커밋 시 version 불일치 → OptimisticLockingFailureException → 트랜잭션 롤백 후 Retry 인터셉터가 포착 → 새 트랜잭션으로 재시도
  // noRetryFor = CustomException: PostException 등 비즈니스 예외는 재시도·복구 과정을 건너뛰고 즉시 전파
  // 없으면 @Recover 타입 불일치로 ClassCastException → 500 응답
  @Retryable(
      retryFor = OptimisticLockingFailureException.class,
      noRetryFor = CustomException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 100, multiplier = 2)
  )
  @Transactional
  public void addLike(Long postId, Long userId) {
    // ① 중복 체크를 가장 먼저: 이미 좋아요를 눌렀다면 Post/User 조회 없이 즉시 예외
    if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
      throw new LikeException(LikeErrorCode.LIKE_ALREADY_EXISTS);
    }

    // ② version 필드가 있는 Post 로딩 — 이 시점의 version이 커밋 시 기준값이 된다
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    // ③ getReferenceById: SELECT 없이 userId를 ID로 가진 프록시 반환
    // JWT 필터를 통과한 userId는 서버가 이미 검증한 값이므로 실제 존재가 보장됨
    // findById()는 User 데이터 전체를 SELECT하지만, Like 저장에는 FK(user_id) 값만 필요
    User user = userRepository.getReferenceById(userId);

    likeRepository.save(new Like(user, post));
    // likeCount 변경 → JPA dirty 감지 → UPDATE post SET like_count=?, version=? WHERE id=? AND version=?
    // 다른 트랜잭션이 먼저 커밋했다면 version 불일치 → OptimisticLockingFailureException
    post.incrementLikeCount();
  }

  @Retryable(
      retryFor = OptimisticLockingFailureException.class,
      noRetryFor = CustomException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 100, multiplier = 2)
  )
  @Transactional
  public void cancelLike(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
      throw new LikeException(LikeErrorCode.LIKE_NOT_FOUND);
    }

    likeRepository.deleteByUserIdAndPostId(userId, postId);
    post.decrementLikeCount();
  }

  // 재시도를 maxAttempts 횟수만큼 소진한 후 최종 호출되는 복구 메서드
  // 반환 타입과 파라미터 타입이 @Retryable 메서드와 일치해야 Spring Retry가 연결한다
  @Recover
  public void recover(OptimisticLockingFailureException e, Long postId, Long userId) {
    throw new LikeException(LikeErrorCode.LIKE_CONCURRENT_CONFLICT);
  }

  // @Recover 매칭 실패 시 500으로 새어나오는 것을 막는 catch-all
  // Spring Retry는 예외 계층상 가장 가까운 @Recover를 선택하므로 위 메서드와 충돌하지 않는다
  @Recover
  public void recover(Exception e, Long postId, Long userId) {
    if (e instanceof RuntimeException re) throw re;
    throw new RuntimeException(e);
  }
}
