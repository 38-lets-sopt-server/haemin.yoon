package org.sopt.controller;

import java.util.List;
import org.sopt.dto.ApiResponse;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.service.PostService;

public class PostController {
  private final PostService postService = new PostService();

  // POST /posts
  public ApiResponse<CreatePostResponse> createPost(CreatePostRequest request) {
    try {
      // return postService.createPost(request);
      CreatePostResponse response = postService.createPost(request);
      return ApiResponse.success(response, response.message);
    } catch (IllegalArgumentException e) {
      // return new CreatePostResponse(null, "🚫 " + e.getMessage());
      return ApiResponse.fail("🚫 " + e.getMessage());
    }
  }

  // GET /posts 📝 과제
  public ApiResponse<List<PostResponse>> getAllPosts() {
    // TODO: postService.getAllPosts() 호출해서 반환
    // return postService.getAllPosts();
    try {
      List<PostResponse> posts = postService.getAllPosts();
      return ApiResponse.success(posts, "전체 게시글 조회 성공");
    } catch (Exception e) {
      return ApiResponse.fail("🚫 전체 게시글 조회 실패");
    }
  }

  // GET /posts/{id} 📝 과제
  public ApiResponse<PostResponse> getPost(Long id) {
    // TODO: postService.getPost(id) 호출, 예외 발생 시 null 반환
//    try {
//      return postService.getPost(id);
//    } catch (IllegalArgumentException e) {
//      System.out.println("🚫 " + e.getMessage());
//      return null;
//    }
    try {
      PostResponse post = postService.getPost(id);
      return ApiResponse.success(post, "게시글 단건 조회 성공");
    } catch (PostNotFoundException | IllegalArgumentException e) {
      return ApiResponse.fail("🚫 " + e.getMessage());
    }
  }

  // PUT /posts/{id} 📝 과제
  public ApiResponse<Void> updatePost(Long id, String newTitle, String newContent) {
    // TODO: postService.updatePost() 호출, 예외 발생 시 에러 메시지 출력
    try {
      postService.updatePost(id, newTitle, newContent);
      // System.out.println("게시글 수정 완료!");
      return ApiResponse.success(null, "게시글 수정 완료!");
    } catch (PostNotFoundException | IllegalArgumentException e) {
      // System.out.println("🚫 " + e.getMessage());
      return ApiResponse.fail("🚫 " + e.getMessage());
    }
  }

  // DELETE /posts/{id} 📝 과제
  public ApiResponse<Void> deletePost(Long id) {
    // TODO: postService.deletePost() 호출, 예외 발생 시 에러 메시지 출력
//    try {
//      postService.deletePost(id);
//      System.out.println("게시글 삭제 완료!");
//    } catch (IllegalArgumentException e) {
//      System.out.println("🚫 " + e.getMessage());
//    }
    try {
      postService.deletePost(id);
      return ApiResponse.success(null, "게시글 삭제 완료!");
    } catch (PostNotFoundException | IllegalArgumentException e) {
      return ApiResponse.fail("🚫 " + e.getMessage());
    }
  }
}