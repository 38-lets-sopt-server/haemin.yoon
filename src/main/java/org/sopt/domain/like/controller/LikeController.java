package org.sopt.domain.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.domain.like.exception.code.LikeSuccessCode;
import org.sopt.domain.like.service.LikeService;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Like", description = "좋아요 관련 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class LikeController {

  private final LikeService likeService;

  public LikeController(LikeService likeService) {
    this.likeService = likeService;
  }

  @Operation(summary = "좋아요 추가", description = "Access Token으로 인증된 유저가 특정 게시글에 좋아요를 누릅니다.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping
  public ResponseEntity<BaseResponse<Void>> addLike(
      @PathVariable Long postId,
      Authentication authentication
  ) {
    // userId를 요청 바디/파라미터로 받지 않고 JWT에서 추출
    // → 클라이언트가 타인의 userId로 좋아요 위장하는 것을 방지
    Long userId = Long.parseLong(authentication.getName());
    likeService.addLike(postId, userId);
    return ResponseEntity
        .status(LikeSuccessCode.LIKE_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(LikeSuccessCode.LIKE_SUCCESS, null));
  }

  @Operation(summary = "좋아요 취소", description = "Access Token으로 인증된 유저가 특정 게시글의 좋아요를 취소합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping
  public ResponseEntity<BaseResponse<Void>> cancelLike(
      @PathVariable Long postId,
      Authentication authentication
  ) {
    Long userId = Long.parseLong(authentication.getName());
    likeService.cancelLike(postId, userId);
    return ResponseEntity
        .status(LikeSuccessCode.LIKE_CANCEL_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(LikeSuccessCode.LIKE_CANCEL_SUCCESS, null));
  }
}
