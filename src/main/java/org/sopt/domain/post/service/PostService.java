package org.sopt.domain.post.service;

import java.time.LocalDateTime;
import java.util.List;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
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
        LocalDateTime.now().toString()
    );

    postRepository.save(post);
    return PostResponse.from(post);
  }

  // READ - 전체 조회
  public List<PostResponse> getAllPosts() {
    return postRepository.findAll()
        .stream()
        .map(PostResponse::from)
        .toList();
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
