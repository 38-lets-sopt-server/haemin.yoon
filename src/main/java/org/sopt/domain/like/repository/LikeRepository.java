package org.sopt.domain.like.repository;

import org.sopt.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

  boolean existsByUserIdAndPostId(Long userId, Long postId);

  // void deleteByUserIdAndPostId(Long userId, Long postId);
  // 개선: 단일 벌크 DELETE (1 query)
  @Modifying
  @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
  void deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
