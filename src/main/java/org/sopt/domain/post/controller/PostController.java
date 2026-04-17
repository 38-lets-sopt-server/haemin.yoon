package org.sopt.domain.post.controller;

import java.util.List;
import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.dto.response.PostResponse;
import org.sopt.domain.post.exception.code.PostSuccessCode;
import org.sopt.domain.post.service.PostService;
import org.sopt.global.response.ApiResponse;
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
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  // POST /posts
  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
      @RequestBody CreatePostRequest request
  ) {
    PostResponse response = postService.createPost(request);

    return ResponseEntity
        .status(PostSuccessCode.POST_CREATE_SUCCESS.getHttpStatus())
        .body(ApiResponse.onSuccess(PostSuccessCode.POST_CREATE_SUCCESS, response));
  }

  // GET /posts
  @GetMapping
  public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts() {
    List<PostResponse> response = postService.getAllPosts();
    return ResponseEntity
        .status(PostSuccessCode.POST_GET_ALL_SUCCESS.getHttpStatus())
        .body(ApiResponse.onSuccess(PostSuccessCode.POST_GET_ALL_SUCCESS, response));
  }

  // GET /posts/{id}
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getPost(
      @PathVariable Long id
  ) {
    PostResponse response = postService.getPost(id);
    return ResponseEntity
        .status(PostSuccessCode.POST_GET_SUCCESS.getHttpStatus())
        .body(ApiResponse.onSuccess(PostSuccessCode.POST_GET_SUCCESS, response));
  }

  // PUT /posts/{id}
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> updatePost(
      @PathVariable Long id,
      @RequestBody UpdatePostRequest request
  ) {
    postService.updatePost(id, request);
    return ResponseEntity
        .status(PostSuccessCode.POST_UPDATE_SUCCESS.getHttpStatus())
        .body(ApiResponse.onSuccess(PostSuccessCode.POST_UPDATE_SUCCESS, null));
  }

  // DELETE /posts/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @PathVariable Long id
  ) {
    postService.deletePost(id);
    return ResponseEntity
        .status(PostSuccessCode.POST_DELETE_SUCCESS.getHttpStatus())
        .body(ApiResponse.onSuccess(PostSuccessCode.POST_DELETE_SUCCESS, null));
  }
}