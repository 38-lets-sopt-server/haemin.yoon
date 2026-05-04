package org.sopt.domain.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.sopt.domain.like.dto.request.LikeRequest;
import org.sopt.domain.like.exception.code.LikeSuccessCode;
import org.sopt.domain.like.service.LikeService;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Like", description = "좋아요 관련 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class LikeController {

  private final LikeService likeService;

  public LikeController(LikeService likeService) {
    this.likeService = likeService;
  }

  @Operation(summary = "좋아요 추가", description = "특정 게시글에 좋아요를 누릅니다. 같은 게시글에 중복 좋아요는 불가합니다.")
  @PostMapping
  public ResponseEntity<BaseResponse<Void>> addLike(
      @PathVariable Long postId,
      @Valid @RequestBody LikeRequest request
  ) {
    likeService.addLike(postId, request.userId());
    return ResponseEntity
        .status(LikeSuccessCode.LIKE_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(LikeSuccessCode.LIKE_SUCCESS, null));
  }

  @Operation(summary = "좋아요 취소", description = "특정 게시글에 누른 좋아요를 취소합니다.")
  @DeleteMapping
  public ResponseEntity<BaseResponse<Void>> cancelLike(
      @PathVariable Long postId,
      @NotNull @RequestParam Long userId
  ) {
    likeService.cancelLike(postId, userId);
    return ResponseEntity
        .status(LikeSuccessCode.LIKE_CANCEL_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(LikeSuccessCode.LIKE_CANCEL_SUCCESS, null));
  }
}
