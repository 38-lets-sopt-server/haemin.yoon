package org.sopt.domain.post.repository;

import java.util.Optional;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// JpaRepository: 기본 CRUD + 페이징
// PostRepositoryCustom: QueryDSL 기반 동적 검색
// Spring Data가 두 인터페이스의 구현을 하나의 프록시로 합성해준다
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  // ManyToOne fetch join은 페이징과 함께 사용해도 안전 (OneToMany와 달리 Cartesian product 없음)
  @Query("SELECT p FROM Post p JOIN FETCH p.user")
  Page<Post> findAllWithUser(Pageable pageable);

  @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.boardType = :boardType")
  Page<Post> findAllByBoardTypeWithUser(@Param("boardType") BoardType boardType, Pageable pageable);

  // 단건 조회 시 user 지연 로딩 방지
  @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
  Optional<Post> findByIdWithUser(@Param("id") Long id);

  // countQuery를 별도로 분리하는 이유:
  // Page<T> 반환 시 Spring Data는 totalElements를 위한 COUNT 쿼리를 자동 생성하는데,
  // JOIN FETCH를 COUNT에 그대로 적용하면 "query specified join fetching, but the owner of the fetched association was not present" 오류 발생.
  // COUNT 쿼리에서는 JOIN FETCH 불필요 — 단순 JOIN으로 충분하고 실제로는 조인도 필요 없어 p만으로 카운팅.
  @Query(
      value = "SELECT p FROM Post p JOIN FETCH p.user " +
              "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))",
      countQuery = "SELECT COUNT(p) FROM Post p " +
                   "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))"
  )
  Page<Post> searchByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}