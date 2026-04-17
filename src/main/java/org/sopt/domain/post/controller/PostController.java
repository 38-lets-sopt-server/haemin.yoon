package org.sopt.controller;

import java.util.List;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  // POST /posts ✅ 같이 구현
  @PostMapping
  public ResponseEntity<CreatePostResponse> createPost(
      @RequestBody CreatePostRequest request
  ) {
    CreatePostResponse response = postService.createPost(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // GET /posts 📝 과제
  @GetMapping
  public ResponseEntity<List<PostResponse>> getAllPosts() {
    //TODO
    return null;
  }

  // GET /posts/{id} 📝 과제
  @GetMapping("/{id}")
  public ResponseEntity<PostResponse> getPost(
      @PathVariable Long id
  ) {
    //TODO
    return null;
  }

  // PUT /posts/{id} 📝 과제
  @PutMapping("/{id}")
  public ResponseEntity<Void> updatePost(
      @PathVariable Long id,
      @RequestBody UpdatePostRequest request
  ) {
    //TODO
    return null;
  }

  // DELETE /posts/{id} 📝 과제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(
      @PathVariable Long id
  ) {
    //TODO
    return null;
  }
}