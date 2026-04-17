package org.sopt.domain.post.service;

import java.time.LocalDateTime;
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
import org.sopt.domain.post.exception.PostNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PostService {
  private final PostRepository postRepository;
  private final PostValidator postValidator;

  public PostService(PostRepository postRepository, PostValidator postValidator) {
    this.postRepository = postRepository;
    this.postValidator = postValidator;
  }

  // CREATE - 게시글 생성
  public PostResponse createPost(CreatePostRequest request) {
    postValidator.validateCreateRequest(request); // 예외 검증 처리

    Post post = new Post(
        postRepository.generateId(),
        request.title(),
        request.content(),
        request.author(),
        LocalDateTime.now().toString(),
        request.boardType()
    );

    postRepository.save(post);
    return PostResponse.from(post);
  }

  // READ - 전체 조회
  public PostPageResponse getAllPosts(int page, int size) {
    // 1. 페이지 요청 값 검증 (page >= 0, size >= 1)
    postValidator.validatePagination(page, size);

    // 2. 전체 게시글 조회 후 DTO로 변환
    List<PostResponse> allPosts = postRepository.findAll()
        .stream()
        .map(PostResponse::from)
        .toList();

    // 3. 전체 데이터 개수 및 총 페이지 수 계산
    int totalElements = allPosts.size();
    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

    // 4. 현재 페이지에 해당하는 시작 인덱스 계산
    int startIndex = page * size;

    // 5. 요청한 페이지가 범위를 벗어난 경우 → 빈 리스트 반환
    if (startIndex >= totalElements) {
      return new PostPageResponse(
          List.of(),
          page,
          size,
          totalElements,
          totalPages,
          page == 0,
          true
      );
    }

    // 6. 현재 페이지의 마지막 인덱스 계산 (전체 범위를 넘지 않도록 보정)
    int endIndex = Math.min(startIndex + size, totalElements);
    // 7. 해당 페이지에 포함되는 게시글 목록 추출
    List<PostResponse> pagedPosts = allPosts.subList(startIndex, endIndex);

    // 8. 첫 페이지 여부, 마지막 페이지 여부 판단
    boolean isFirst = page == 0;
    boolean isLast = page >= totalPages - 1;

    // 9. 페이지 정보 + 게시글 목록을 함께 응답 DTO로 반환
    return new PostPageResponse(
        pagedPosts,
        page,
        size,
        totalElements,
        totalPages,
        isFirst,
        isLast
    );
  }
//  // 기존 페이지 없이 전체 조회 코드
//  public List<PostResponse> getAllPosts() {
//    return postRepository.findAll()
//        .stream()
//        .map(PostResponse::from)
//        .toList();
//  }

  // READ - 게시글 종류별 전체 조회
  public PostPageResponse getPostsByBoardType(BoardType boardType, int page, int size) {
    postValidator.validatePagination(page, size);

    List<PostResponse> filteredPosts = postRepository.findAllByBoardType(boardType)
        .stream()
        .map(PostResponse::from)
        .toList();

    int totalElements = filteredPosts.size();
    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    int startIndex = page * size;

    if (startIndex >= totalElements) {
      return new PostPageResponse(
          List.of(),
          page,
          size,
          totalElements,
          totalPages,
          page == 0,
          true
      );
    }

    int endIndex = Math.min(startIndex + size, totalElements);
    List<PostResponse> pagedPosts = filteredPosts.subList(startIndex, endIndex);

    boolean isFirst = page == 0;
    boolean isLast = page >= totalPages - 1;

    return new PostPageResponse(
        pagedPosts,
        page,
        size,
        totalElements,
        totalPages,
        isFirst,
        isLast
    );
  }

  // READ - 단건 조회
  public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    return PostResponse.from(post);
  }

  // UPDATE - 수정
  public void updatePost(Long id, UpdatePostRequest request) {
    postValidator.validateUpdateRequest(request); // 예외 검증

    Post post = postRepository.findById(id)
        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

    post.update(request.title(), request.content());
  }

  // DELETE - 삭제
  public void deletePost(Long id) {
    Post post = postRepository.findById(id)
        //.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        .orElseThrow(PostNotFoundException::new);

    postRepository.deleteById(post.getId());
  }
}
