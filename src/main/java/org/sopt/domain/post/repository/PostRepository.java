package org.sopt.domain.post.repository;

import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  // 특정 게시판 타입의 글들을 조회하는 메서드
  @Query("select p from Post p join fetch p.user where p.boardType = :boardType")
  Page<Post> findAllByBoardTypeWithUser(@Param("boardType") BoardType boardType, Pageable pageable);
}