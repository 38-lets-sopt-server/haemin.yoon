package org.sopt.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.domain.post.dto.request.CreatePostRequest;
import org.sopt.domain.post.dto.request.UpdatePostRequest;
import org.sopt.domain.post.dto.response.PostPageResponse;
import org.sopt.domain.post.dto.response.PostResponse;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.exception.code.PostSuccessCode;
import org.sopt.domain.post.service.PostService;
import org.sopt.global.response.BaseResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post", description = "게시글 관련 API")
@Validated  // @RequestParam의 @NotBlank 등 제약 어노테이션을 활성화 (@Valid는 @RequestBody에만 동작)
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @Operation(summary = "게시글 생성", description = "userId를 받아 새로운 게시글을 작성합니다.")
  @PostMapping
  public ResponseEntity<BaseResponse<PostResponse>> createPost(
      @Valid @RequestBody CreatePostRequest request
  ) {
    PostResponse response = postService.createPost(request);

    return ResponseEntity
        .status(PostSuccessCode.POST_CREATE_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_CREATE_SUCCESS, response));
  }

  @Operation(summary = "전체 게시글 조회(페이징)", description = "페이지 번호와 사이즈를 받아 전체 게시글을 조회합니다.")
  @GetMapping
  public ResponseEntity<BaseResponse<PostPageResponse>> getAllPosts(
      @RequestParam(required = false) BoardType boardType,
      @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    PostPageResponse response = postService.getPosts(boardType, pageable);
    return ResponseEntity
        .status(PostSuccessCode.POST_GET_ALL_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_GET_ALL_SUCCESS, response));
  }

  @Operation(summary = "게시글 단건 조회", description = "ID를 통해 특정 게시글을 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<PostResponse>> getPost(
      @PathVariable Long id
  ) {
    PostResponse response = postService.getPost(id);
    return ResponseEntity
        .status(PostSuccessCode.POST_GET_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_GET_SUCCESS, response));
  }

  @Operation(summary = "게시글 수정", description = "기존 게시글의 제목과 내용을 수정합니다.")
  @PutMapping("/{id}")
  public ResponseEntity<BaseResponse<PostResponse>> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody UpdatePostRequest request
  ) {
    PostResponse response = postService.updatePost(id, request);
    return ResponseEntity
        .status(PostSuccessCode.POST_UPDATE_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_UPDATE_SUCCESS, response));
  }

  // /search가 /{id}보다 먼저 선언되어야 "search"가 PathVariable id로 잘못 매핑되지 않는다
  @Operation(
      summary = "게시글 동적 검색",
      description = "제목 키워드와 작성자 닉네임을 선택적으로 조합해 검색합니다. 둘 다 생략하면 전체 조회와 동일합니다."
  )
  @GetMapping("/search")
  public ResponseEntity<BaseResponse<PostPageResponse>> searchPosts(
      @RequestParam(required = false) String titleKeyword,
      @RequestParam(required = false) String authorNickname,
      @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    PostPageResponse response = postService.searchPosts(titleKeyword, authorNickname, pageable);
    return ResponseEntity
        .status(PostSuccessCode.POST_SEARCH_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_SEARCH_SUCCESS, response));
  }

  @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<Void>> deletePost(
      @PathVariable Long id
  ) {
    postService.deletePost(id);
    return ResponseEntity
        .status(PostSuccessCode.POST_DELETE_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(PostSuccessCode.POST_DELETE_SUCCESS, null));
  }
}
