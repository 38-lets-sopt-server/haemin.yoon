package org.sopt.controller;

import java.util.List;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.service.PostService;

public class PostController {
  private final PostService postService = new PostService();

  // POST /posts
  public CreatePostResponse createPost(CreatePostRequest request) {
    try {
      return postService.createPost(request);
    } catch (IllegalArgumentException e) {
      return new CreatePostResponse(null, "🚫 " + e.getMessage());
    }
  }

  // GET /posts 📝 과제
  public List<PostResponse> getAllPosts() {
    // TODO: postService.getAllPosts() 호출해서 반환
    return postService.getAllPosts();
  }

  // GET /posts/{id} 📝 과제
  public PostResponse getPost(Long id) {
    // TODO: postService.getPost(id) 호출, 예외 발생 시 null 반환
    try {
      return postService.getPost(id);
    } catch (IllegalArgumentException e) {
      System.out.println("🚫 " + e.getMessage());
      return null;
    }
  }

  // PUT /posts/{id} 📝 과제
  public void updatePost(Long id, String newTitle, String newContent) {
    // TODO: postService.updatePost() 호출, 예외 발생 시 에러 메시지 출력
    try {
      postService.updatePost(id, newTitle, newContent);
      System.out.println("게시글 수정 완료!");
    } catch (IllegalArgumentException e) {
      System.out.println("🚫 " + e.getMessage());
    }
  }

  // DELETE /posts/{id} 📝 과제
  public void deletePost(Long id) {
    // TODO: postService.deletePost() 호출, 예외 발생 시 에러 메시지 출력
    try {
      postService.deletePost(id);
      System.out.println("게시글 삭제 완료!");
    } catch (IllegalArgumentException e) {
      System.out.println("🚫 " + e.getMessage());
    }
  }
}