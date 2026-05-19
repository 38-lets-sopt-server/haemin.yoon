package org.sopt.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.domain.auth.dto.request.ReissueRequest;
import org.sopt.domain.auth.dto.response.TokenResponse;
import org.sopt.domain.auth.exception.code.AuthSuccessCode;
import org.sopt.domain.auth.service.AuthService;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
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
            @RequestParam String email,
            @RequestParam String password
    ) {
        TokenResponse tokens = authService.login(email, password);
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
}
