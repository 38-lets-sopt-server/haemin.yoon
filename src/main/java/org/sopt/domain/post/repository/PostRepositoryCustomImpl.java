package org.sopt.domain.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.sopt.domain.post.entity.Post;
import org.sopt.domain.post.entity.QPost;
import org.sopt.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import com.querydsl.core.types.dsl.BooleanExpression;

// @Repository 없음 — Spring Data가 '{프래그먼트명}Impl' 규칙으로 자동 탐지하고 DI를 처리한다.
// @Repository를 붙이면 별도 Bean으로 이중 등록되어 문제가 생길 수 있다.
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

  private static final QPost post = QPost.post;
  private static final QUser user = QUser.user;

  private final JPAQueryFactory queryFactory;

  public PostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public Page<Post> searchDynamically(String titleKeyword, String authorNickname, Pageable pageable) {
    // Content 쿼리: fetchJoin으로 user를 함께 로딩 (N+1 방지, PostResponse.author 필드용)
    List<Post> content = queryFactory
        .selectFrom(post)
        .join(post.user, user).fetchJoin()
        .where(
            titleContains(titleKeyword),      // null이면 QueryDSL이 조건에서 자동 제외
            authorNicknameContains(authorNickname)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(toOrderSpecifiers(pageable.getSort()))
        .fetch();

    // Count 쿼리: fetchJoin 없이 순수 카운팅
    // authorNickname 조건이 있을 때만 user JOIN — 없으면 불필요한 JOIN 생략
    JPAQuery<Long> countQuery = queryFactory
        .select(post.count())
        .from(post);

    if (StringUtils.hasText(authorNickname)) {
      countQuery.join(post.user, user);
    }

    long total = countQuery
        .where(
            titleContains(titleKeyword),
            authorNicknameContains(authorNickname)
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }

  // BooleanExpression을 반환하고 null을 허용하는 이유:
  // QueryDSL의 where(expr...)는 null인 인자를 AND에서 조용히 제외한다.
  // BooleanBuilder 대비 각 조건이 독립적이어서 테스트와 조합이 쉽다.
  private BooleanExpression titleContains(String keyword) {
    return StringUtils.hasText(keyword) ? post.title.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression authorNicknameContains(String nickname) {
    return StringUtils.hasText(nickname) ? post.user.nickname.containsIgnoreCase(nickname) : null;
  }

  // Pageable의 Sort를 QueryDSL OrderSpecifier 배열로 변환
  // PathBuilder를 쓰는 이유: 필드명을 런타임 문자열로 받아 동적으로 경로를 구성해야 하기 때문.
  // QPost.post.createdAt처럼 정적으로 쓰면 새 정렬 조건이 생길 때마다 코드 수정이 필요하다.
  @SuppressWarnings("unchecked")
  private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
    if (sort.isUnsorted()) {
      return new OrderSpecifier<?>[]{ post.createdAt.desc() };
    }

    PathBuilder<Post> postPath = new PathBuilder<>(Post.class, "post");

    return sort.stream()
        .map(order -> {
          ComparablePath<Comparable> path =
              postPath.getComparable(order.getProperty(), Comparable.class);
          return order.isAscending() ? path.asc() : path.desc();
        })
        .toArray(OrderSpecifier[]::new);
  }
}
