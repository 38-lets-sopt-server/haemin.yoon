package org.sopt.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.sopt.domain.auth.exception.code.AuthErrorCode;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 인증은 됐지만 Spring Security 레벨에서 접근이 거부될 때 호출된다.
 * 예: @PreAuthorize, @Secured 등 메서드 보안 어노테이션 실패
 *
 * 게시글 소유권 검증(POST_FORBIDDEN)처럼 서비스 레이어에서 던지는 예외는
 * GlobalExceptionHandler가 처리하므로 이 핸들러를 거치지 않는다.
 * 이 핸들러는 Spring Security 자체가 막는 경우(필터·인터셉터 레벨)에만 호출된다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(AuthErrorCode.AUTH_FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse<Void> body = BaseResponse.onFailure(
                AuthErrorCode.AUTH_FORBIDDEN.getCode(),
                AuthErrorCode.AUTH_FORBIDDEN.getMessage()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
