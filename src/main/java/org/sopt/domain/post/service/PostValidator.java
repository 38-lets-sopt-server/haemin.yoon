package org.sopt.domain.post.service;

import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.exception.PostException;
import org.sopt.domain.post.exception.code.PostErrorCode;

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
}