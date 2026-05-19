package org.sopt.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.sopt.domain.auth.exception.code.AuthErrorCode;
import org.sopt.global.response.BaseResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증되지 않은 요청이 보호된 엔드포인트에 접근할 때 호출된다.
 *
 * Spring Security는 필터 단에서 예외를 처리하므로 GlobalExceptionHandler에 도달하지 않는다.
 * 이 클래스를 등록하지 않으면 Spring Security 기본 동작(HTML 에러 페이지 또는 규격 없는 403)이 반환되어
 * API 응답 형식이 깨진다 — 우리 BaseResponse 포맷으로 통일하기 위해 직접 JSON을 작성한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(AuthErrorCode.AUTH_UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse<Void> body = BaseResponse.onFailure(
                AuthErrorCode.AUTH_UNAUTHORIZED.getCode(),
                AuthErrorCode.AUTH_UNAUTHORIZED.getMessage()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
