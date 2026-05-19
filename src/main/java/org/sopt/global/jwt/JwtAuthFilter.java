package org.sopt.global.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            try {
                Long userId = jwtService.verifyAndGetUserId(token);

                // principal에 userId(String)를 담아 SecurityContext에 저장
                // 이후 컨트롤러에서 authentication.getName()으로 userId를 꺼낼 수 있음
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId), null, Collections.emptyList());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (IllegalArgumentException | JWTVerificationException e) {
                // 유효하지 않은 토큰은 인증 없이 다음 필터로 넘김
                // 예외를 여기서 던지지 않는 이유: /api/v1/auth/login 같이 인증이 필요 없는
                // 엔드포인트도 이 필터를 통과하기 때문 — 인증 필요 여부는 SecurityConfig에서 판단
            }
        }

        filterChain.doFilter(request, response);
    }
}
