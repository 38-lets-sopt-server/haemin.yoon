package org.sopt.domain.post.service;

import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.exception.PostException;
import org.sopt.domain.post.exception.code.PostErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PostValidator {

  // 기존 PostService 안에 있던 제목/내용 검증을 여기로 뺀다.

  // 생성 시 검증
  public void validateCreateRequest(CreatePostRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new PostException(PostErrorCode.POST_BAD_REQUEST);
    }
    if (request.content() == null || request.content().isBlank()) {
      throw new PostException(PostErrorCode.POST_BAD_REQUEST);
    }
    if (request.author() == null || request.author().isBlank()) {
      throw new PostException(PostErrorCode.POST_BAD_REQUEST);
    }
  }

  // 수정 시 검증
  public void validateUpdateRequest(UpdatePostRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new PostException(PostErrorCode.POST_BAD_REQUEST);
    }
    if (request.content() == null || request.content().isBlank()) {
      throw new PostException(PostErrorCode.POST_BAD_REQUEST);
    }
  }

  // 잘못된 페이지 요청(page < 0 또는 size <= 0) 검증
  public void validatePagination(int page, int size) {
    if (page < 0 || size <= 0) { // page가 0보다 작거나 size가 0보다 작거나 같으면 잘못된 요청이므로 예외를 발생
      throw new PostException(PostErrorCode.POST_INVALID_PAGINATION);
    }
  }
}