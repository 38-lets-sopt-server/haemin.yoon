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

  @Transactional
  public PostResponse createPost(CreatePostRequest request) {
    User user = userRepository.findById(request.userId())
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

  // Pageable은 컨트롤러에서 Spring이 만들어주고, Repository에 그대로 넘겨 DB 레벨에서 정렬/페이징 처리
  // likeCount는 Post 엔티티에 비정규화되어 있으므로 별도 COUNT 쿼리 없이 JOIN FETCH 1번으로 해결
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
  public PostResponse updatePost(Long id, UpdatePostRequest request) {
    Post post = postRepository.findByIdWithUser(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    post.update(request.title(), request.content());
    return PostResponse.from(post);
  }

  @Transactional
  public void deletePost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    postRepository.delete(post);
  }

  // titleKeyword, authorNickname 모두 null/빈 문자열 허용 — QueryDSL이 동적으로 조건을 조합
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
}
