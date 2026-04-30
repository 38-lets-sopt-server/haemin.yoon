package org.sopt.domain.post.service;

import java.util.List;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.dto.response.PostPageResponse;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;
import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.response.PostResponse;
import org.sopt.domain.post.exception.PostException;
import org.sopt.domain.post.exception.code.PostErrorCode;
import org.sopt.domain.post.repository.PostRepository;
import org.sopt.domain.user.entity.User;
import org.sopt.domain.user.repository.UserRepository;
import org.sopt.global.exception.CustomException;
import org.sopt.global.exception.code.GlobalErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public PostService(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  // CREATE - 게시글 생성
  @Transactional  // 저장 → DB 변경 발생 → 트랜잭션 커밋 시 반영
  public PostResponse createPost(CreatePostRequest request) {
    // 1. 작성자 존재 여부 확인
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new CustomException(GlobalErrorCode.COMMON_BAD_REQUEST));

    Post post = new Post(
        null, // ID는 DB에서 자동 생성(IDENTITY)
        request.title(),
        request.content(),
        user,
        request.boardType()
    );

    Post savedPost = postRepository.save(post);
    return PostResponse.from(savedPost);
  }

  // READ - 전체 조회 (boardtype 없으면 전체 조회, 있으면 해당 타입의 게시글 전체조회)
  // Pageable은 컨트롤러에서 Spring이 만들어주고, Repository에 그대로 넘겨 DB 레벨에서 정렬/페이징 처리
  @Transactional(readOnly = true)
  public PostPageResponse getPosts(BoardType boardType, Pageable pageable) {
    Page<Post> postPage = (boardType != null)
        ? postRepository.findAllByBoardTypeWithUser(boardType, pageable)
        : postRepository.findAll(pageable);

    List<PostResponse> content = postPage.getContent().stream()
        .map(PostResponse::from)
        .toList();

    return new PostPageResponse(
        content,
        postPage.getNumber(),
        postPage.getSize(),
        (int) postPage.getTotalElements(),
        postPage.getTotalPages(),
        postPage.isFirst(),
        postPage.isLast()
    );
  }

  // READ - 단건 조회
  @Transactional(readOnly = true)  // 조회 전용 → 더티 체킹 안 함
  public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    return PostResponse.from(post);
  }

  // UPDATE - 수정
  @Transactional
  public PostResponse updatePost(Long id, UpdatePostRequest request) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    post.update(request.title(), request.content());
    return PostResponse.from(post);
  }

  // DELETE - 삭제
  @Transactional
  public void deletePost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    postRepository.deleteById(post.getId());
  }
}
