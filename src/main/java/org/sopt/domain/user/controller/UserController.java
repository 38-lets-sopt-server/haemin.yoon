package org.sopt.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.domain.user.dto.request.CreateUserRequest;
import org.sopt.domain.user.dto.response.CreateUserResponse;
import org.sopt.domain.user.dto.response.UserResponse;
import org.sopt.domain.user.exception.UserException;
import org.sopt.domain.user.exception.code.UserErrorCode;
import org.sopt.domain.user.exception.code.UserSuccessCode;
import org.sopt.domain.user.service.UserService;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "회원 가입", description = "닉네임, 이메일, 비밀번호를 받아 새로운 유저를 생성합니다.")
  @PostMapping
  public ResponseEntity<BaseResponse<CreateUserResponse>> join(
      @Valid @RequestBody CreateUserRequest request
  ) {
    CreateUserResponse response = userService.join(request);
    return ResponseEntity
        .status(UserSuccessCode.USER_JOIN_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(UserSuccessCode.USER_JOIN_SUCCESS, response));
  }

  @Operation(summary = "내 정보 조회", description = "Access Token으로 인증된 유저의 정보를 반환합니다.")
  @SecurityRequirement(name = "bearerAuth")  // Swagger에서 자물쇠 아이콘 활성화
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<UserResponse>> getMe(Authentication authentication) {
    // SecurityConfig에서 anyRequest().authenticated()로 보호하지만
    // 혹시 필터가 누락될 경우를 대비한 방어 코드
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new UserException(UserErrorCode.USER_NOT_FOUND);
    }

    // JwtAuthFilter에서 principal에 userId(String)를 담아뒀으므로 파싱해서 사용
    Long userId = Long.parseLong(authentication.getName());
    UserResponse response = userService.getUserById(userId);
    return ResponseEntity
        .status(UserSuccessCode.USER_GET_ME_SUCCESS.getHttpStatus())
        .body(BaseResponse.onSuccess(UserSuccessCode.USER_GET_ME_SUCCESS, response));
  }
}
