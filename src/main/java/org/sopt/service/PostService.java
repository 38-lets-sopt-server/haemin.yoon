package org.sopt.service;

import java.util.List;
import java.util.stream.Collectors;
import org.sopt.domain.Post;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.repository.PostRepository;

public class PostService {
  private final PostRepository postRepository = new PostRepository();
  private final PostValidator postValidator = new PostValidator();

  // CREATE
  public CreatePostResponse createPost(CreatePostRequest request) {
//    if (request.title == null || request.title.isBlank()) {
//      throw new IllegalArgumentException("제목은 필수입니다!");
//    }
//    if (request.content == null || request.content.isBlank()) {
//      throw new IllegalArgumentException("내용은 필수입니다!");
//    }
    // 예외 처리 함수로 대체
    postValidator.validateCreate(request.title, request.content, request.author);

    String createdAt = java.time.LocalDateTime.now().toString();
    Post post = new Post(postRepository.generateId(), request.title, request.content, request.author, createdAt);
    postRepository.save(post);
    return new CreatePostResponse(post.getId(), "게시글 등록 완료!");
  }

  // READ - 전체 📝 과제
  public List<PostResponse> getAllPosts() {
    // TODO
    return postRepository.findAll()
        .stream()
        .map(PostResponse::new)
        .collect(Collectors.toList());
  }

  // READ - 단건 📝 과제
  public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id);

    if (post == null) {
      // throw new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다.");
      throw new PostNotFoundException(id);
    }

    return new PostResponse(post);
  }

  // UPDATE 📝 과제
  public void updatePost(Long id, String newTitle, String newContent) {
    Post post = postRepository.findById(id);
    if (post == null) {
      throw new PostNotFoundException(id);
    }

//    if (post == null) {
//      throw new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다.");
//    }
//    if (newTitle == null || newTitle.isBlank()) {
//      throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
//    }
//    if (newContent == null || newContent.isBlank()) {
//      throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
//    }
    // 예외 검증 함수로 대체
    postValidator.validateUpdate(newTitle, newContent);
    post.update(newTitle, newContent);
  }

  // DELETE 📝 과제
  public void deletePost(Long id) {
    Post post = postRepository.findById(id);

    if (post == null) {
      // throw new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다.");
      throw new PostNotFoundException(id);
    }

    postRepository.deleteById(id);
  }
}
