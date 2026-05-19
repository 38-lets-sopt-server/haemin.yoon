package org.sopt.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    // 토큰 값 자체는 유일해야 조회/무효화가 정확함
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    protected RefreshToken() {}

    private RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken of(Long userId, String token, long expiresInSeconds) {
        return new RefreshToken(
                userId,
                token,
                LocalDateTime.now().plusSeconds(expiresInSeconds)
        );
    }

    /**
     * Refresh Token Rotation: 재발급 시 동일 row를 업데이트해 항상 유저당 토큰 1개를 유지
     * 새 row를 INSERT하지 않으므로 userId UNIQUE 제약 없이도 중복 발급 방지 효과
     */
    public void rotate(String newToken, long expiresInSeconds) {
        this.token = newToken;
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
