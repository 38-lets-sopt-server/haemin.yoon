package org.sopt.domain.post.service;

import java.util.List;
import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.dto.response.PostPageResponse;
import org.sopt.domain.post.dto.response.PostResponse;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;
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

  @Transactional
  public PostResponse createPost(Long userId, CreatePostRequest request) {
    // userId는 컨트롤러에서 JWT로 검증된 값 — 클라이언트 입력값 아님
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.COMMON_BAD_REQUEST));

    Post savedPost = postRepository.save(new Post(
        null,
        request.title(),
        request.content(),
        user,
        request.boardType()
    ));
    return PostResponse.from(savedPost);
  }

  @Transactional(readOnly = true)
  public PostPageResponse getPosts(BoardType boardType, Pageable pageable) {
    Page<Post> postPage = (boardType != null)
        ? postRepository.findAllByBoardTypeWithUser(boardType, pageable)
        : postRepository.findAllWithUser(pageable);

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

  @Transactional(readOnly = true)
  public PostResponse getPost(Long id) {
    Post post = postRepository.findByIdWithUser(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    return PostResponse.from(post);
  }

  @Transactional
  public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
    Post post = postRepository.findByIdWithUser(postId)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    // 인증(Authentication) ≠ 인가(Authorization)
    // JWT 검증으로 "누구인지"는 확인했지만, "이 게시글을 수정할 권한이 있는지"는 별도로 확인해야 함
    validateOwnership(post, userId);

    post.update(request.title(), request.content());
    return PostResponse.from(post);
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    // findByIdWithUser: user JOIN FETCH — 소유권 체크를 위해 user 필드가 필요
    Post post = postRepository.findByIdWithUser(postId)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    validateOwnership(post, userId);
    postRepository.delete(post);
  }

  @Transactional(readOnly = true)
  public PostPageResponse searchPosts(String titleKeyword, String authorNickname, Pageable pageable) {
    Page<Post> postPage = postRepository.searchDynamically(titleKeyword, authorNickname, pageable);

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

  // 게시글 작성자와 요청자가 다르면 403 Forbidden
  // POST_NOT_FOUND(404)보다 먼저 호출되므로, 존재하지 않는 글이면 이 메서드 이전에 이미 예외가 발생함
  private void validateOwnership(Post post, Long userId) {
    if (!post.getUser().getId().equals(userId)) {
      throw new PostException(PostErrorCode.POST_FORBIDDEN);
    }
  }
}
