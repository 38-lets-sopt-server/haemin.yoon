package org.sopt.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final long accessTokenExpiresInSeconds;
    private final long refreshTokenExpiresInSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expires-in-seconds:1800}") long accessTokenExpiresInSeconds,
            @Value("${security.jwt.refresh-token-expires-in-seconds:1209600}") long refreshTokenExpiresInSeconds
    ) {
        // HMAC256: 서버만 아는 secret key로 서명 — 서버가 직접 검증 가능 (비대칭 키 불필요)
        this.algorithm = Algorithm.HMAC256(secret);
        this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
        this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
    }

    /**
     * Access Token 발급
     * subject에 userId, claim에 email을 담아 서버 식별 및 추가 정보 조회 없이 바로 활용할 수 있게 함
     */
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("email", email)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessTokenExpiresInSeconds)))
                .sign(algorithm);
    }

    /**
     * Refresh Token 발급
     * Access Token 재발급 용도로만 쓰이므로 subject(userId)만 담음
     */
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(refreshTokenExpiresInSeconds)))
                .sign(algorithm);
    }

    /**
     * 토큰 검증 후 userId 추출
     * 만료/위변조 시 JWT 라이브러리가 JWTVerificationException을 던짐
     */
    public Long verifyAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰이 없습니다.");
        }
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("JWT의 유저 정보가 올바르지 않습니다.");
        }
    }
}
