package org.sopt.global.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final AccessTokenBlacklistService blacklistService;

    public JwtAuthFilter(JwtService jwtService, AccessTokenBlacklistService blacklistService) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
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

                // 서명·만료가 유효해도 로그아웃된 토큰이면 인증 거부
                // 블랙리스트 체크는 JWT 검증 이후에 수행 — 위변조·만료 토큰은 어차피 위에서 걸러짐
                try {
                    if (blacklistService.isBlacklisted(token)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (RedisConnectionFailureException e) {
                    // Redis 장애 시 fail-open: 블랙리스트 확인을 건너뛰고 인증을 허용한다.
                    // fail-closed(인증 거부)로 바꾸면 보안은 강해지지만 Redis 장애가 전체 서비스 중단으로 이어짐.
                    // 로그아웃 토큰이 일시적으로 유효해지는 위험보다 가용성을 우선하는 판단.
                    log.warn("Redis 연결 실패로 블랙리스트 확인을 건너뜁니다: {}", e.getMessage());
                }

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
