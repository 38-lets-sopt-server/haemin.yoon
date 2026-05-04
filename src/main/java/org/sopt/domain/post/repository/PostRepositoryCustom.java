package org.sopt.domain.post.repository;

import org.sopt.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  // 제목/작성자 닉네임을 선택적으로 조합한 동적 검색 — null/빈 문자열 조건은 쿼리에서 제외된다
  Page<Post> searchDynamically(String titleKeyword, String authorNickname, Pageable pageable);
}
