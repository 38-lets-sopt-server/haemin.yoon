package org.sopt.global.config;

import org.sopt.global.jwt.JwtAccessDeniedHandler;
import org.sopt.global.jwt.JwtAuthFilter;
import org.sopt.global.jwt.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize, @PostAuthorize 등 메서드 레벨 보안 활성화
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // REST API는 CSRF 공격 대상이 아님 (쿠키 기반 세션 없음) — JWT Bearer 토큰 방식 사용
            .csrf(csrf -> csrf.disable())

            // 서버에 세션을 저장하지 않음 — JWT가 상태를 대신 관리
            // STATELESS로 설정하면 Spring Security가 세션을 절대 생성·사용하지 않음
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // ── 인증 불필요 (공개 엔드포인트) ──────────────────────────────
                // 로그인·재발급은 토큰을 발급하는 진입점이므로 토큰 없이 접근 가능해야 함
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/reissue").permitAll()
                // Google 인가 코드를 받아 JWT를 발급하는 진입점 — 토큰 없이 접근 가능해야 함
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/google").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/google/authorize").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/google/callback").permitAll()
                .requestMatchers("/oauth-test.html").permitAll()

                // 회원가입도 토큰 없이 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()

                // 게시글·검색 조회는 비로그인 접근 허용 (에브리타임 스타일)
                // HttpMethod.GET으로 좁혀서 POST /api/v1/posts(게시글 작성)는 인증 필요 상태 유지
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()

                // Swagger UI — 개발·테스트 편의를 위해 허용
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // ── 인증 필요 ────────────────────────────────────────────────────
                // 위에서 permitAll()로 명시된 것 외 모든 요청(POST/PUT/DELETE 게시글, 좋아요 등)
                .anyRequest().authenticated()
            )

            // Spring Security 기본 인증 필터 앞에 JWT 필터 삽입
            // JwtAuthFilter → UsernamePasswordAuthenticationFilter 순으로 실행됨
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // 필터 단에서 발생하는 예외를 우리 BaseResponse 형식으로 통일
            // 미등록 시 Spring Security 기본 동작(규격 없는 HTML/JSON)이 반환되어 API 응답 형식이 깨짐
            .exceptionHandling(ex -> ex
                    // 토큰 없음·만료 → 401
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    // 인증은 됐지만 Spring Security 레벨에서 거부 → 403
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            );

        return http.build();
    }

    /**
     * BCrypt 해시 함수 기반 PasswordEncoder 빈 등록.
     *
     * BCrypt를 선택한 이유:
     *  1. 자동 솔트 내장: encode() 호출마다 랜덤 솔트가 생성되어 동일 비밀번호도 매번 다른 해시 생성
     *     → 레인보우 테이블 공격 무력화
     *  2. 단방향 해시: 해시에서 원문 복원 불가 → DB 유출 시에도 비밀번호 안전
     *  3. 연산 비용 조절 가능: 기본 strength(cost factor) 10은 해시 1회에 ~100ms 소요
     *     → 브루트포스 시도 속도를 실질적으로 제한
     *
     * matches(rawPassword, encodedPassword)가 솔트 추출·재해시·비교를 내부적으로 처리하므로
     * 개발자가 솔트를 직접 관리할 필요 없음.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
