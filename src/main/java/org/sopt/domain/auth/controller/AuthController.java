package org.sopt.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.validation.Valid;
import org.sopt.domain.auth.dto.request.GoogleLoginRequest;
import org.sopt.domain.auth.dto.request.LoginRequest;
import org.sopt.domain.auth.dto.request.ReissueRequest;
import org.sopt.domain.auth.dto.response.TokenResponse;
import org.sopt.domain.auth.exception.code.AuthSuccessCode;
import org.sopt.domain.auth.service.AuthService;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. Access Token(30분)과 Refresh Token(2주)을 발급합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse tokens = authService.login(request.email(), request.password());
        return ResponseEntity
                .status(AuthSuccessCode.AUTH_LOGIN_SUCCESS.getHttpStatus())
                .body(BaseResponse.onSuccess(AuthSuccessCode.AUTH_LOGIN_SUCCESS, tokens));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "만료된 Access Token을 Refresh Token으로 재발급합니다. " +
                          "Refresh Token Rotation 방식으로 Refresh Token도 함께 교체됩니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        TokenResponse tokens = authService.reissue(request.refreshToken());
        return ResponseEntity
                .status(AuthSuccessCode.AUTH_REISSUE_SUCCESS.getHttpStatus())
                .body(BaseResponse.onSuccess(AuthSuccessCode.AUTH_REISSUE_SUCCESS, tokens));
    }

    @Operation(
            summary = "Google 소셜 로그인",
            description = "Google 인가 코드로 로그인합니다. 신규 유저는 자동으로 회원가입됩니다. " +
                          "Access Token(30분)과 Refresh Token(2주)을 발급합니다."
    )
    @PostMapping("/google")
    public ResponseEntity<BaseResponse<TokenResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        TokenResponse tokens = authService.googleLogin(request.code(), request.redirectUri());
        return ResponseEntity
                .status(AuthSuccessCode.AUTH_GOOGLE_LOGIN_SUCCESS.getHttpStatus())
                .body(BaseResponse.onSuccess(AuthSuccessCode.AUTH_GOOGLE_LOGIN_SUCCESS, tokens));
    }

    // ── 개발/테스트 전용 ──────────────────────────────────────────────────────
    @Operation(summary = "Google 로그인 화면으로 이동 (개발용)", hidden = true)
    @GetMapping("/google/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        // client_id 등 민감한 값을 서버에서 조립해 프론트에 노출하지 않음
        response.sendRedirect(authService.getGoogleAuthorizationUrl());
    }

    // 프론트엔드가 없는 환경에서 Google OAuth를 테스트하기 위한 콜백 엔드포인트.
    // 실제 서비스에서는 프론트가 code를 받아 POST /api/v1/auth/google을 호출하므로 이 엔드포인트 불필요.
    @Operation(summary = "Google OAuth 콜백 (개발용)", hidden = true)
    @GetMapping("/google/callback")
    public ResponseEntity<BaseResponse<TokenResponse>> googleCallback(@RequestParam String code) {
        String redirectUri = authService.getGoogleCallbackUri();
        TokenResponse tokens = authService.googleLogin(code, redirectUri);
        return ResponseEntity
                .status(AuthSuccessCode.AUTH_GOOGLE_LOGIN_SUCCESS.getHttpStatus())
                .body(BaseResponse.onSuccess(AuthSuccessCode.AUTH_GOOGLE_LOGIN_SUCCESS, tokens));
    }

    @Operation(
            summary = "로그아웃",
            description = "DB에서 Refresh Token을 삭제하고, 현재 Access Token을 블랙리스트에 등록합니다. " +
                          "블랙리스트에 등록된 토큰은 만료 전이라도 사용할 수 없습니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long userId = Long.parseLong(authentication.getName());

        // "Bearer " 접두사를 제거해 순수 토큰 문자열만 추출
        // JwtAuthFilter에서 이미 검증된 토큰이므로 여기서는 파싱만 수행
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION)
                .substring("Bearer ".length())
                .trim();

        authService.logout(userId, accessToken);

        return ResponseEntity
                .status(AuthSuccessCode.AUTH_LOGOUT_SUCCESS.getHttpStatus())
                .body(BaseResponse.onSuccess(AuthSuccessCode.AUTH_LOGOUT_SUCCESS, null));
    }
}
