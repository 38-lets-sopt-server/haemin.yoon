package org.sopt.global.jwt;

import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 로그아웃된 Access Token을 Redis 블랙리스트로 관리한다.
 *
 * ── 이전 구현: In-memory (ConcurrentHashMap) ─────────────────────────────
 * 서버 재시작 시 초기화, 다중 인스턴스 환경에서 공유 불가, @Scheduled 정리 필요
 *
 * ── 현재 구현: Redis ─────────────────────────────────────────────────────
 * SET blacklist:<token> "1" EX <남은 초>
 * - TTL이 만료되면 Redis가 자동으로 키를 삭제 → @Scheduled 정리 불필요
 * - 서버 재시작과 무관하게 블랙리스트 유지
 * - 다중 인스턴스 환경에서도 공유됨 (로드 밸런서 뒤 여러 서버 가능)
 */
@Service
public class AccessTokenBlacklistService {

    // 키 충돌 방지를 위한 네임스페이스 접두사
    // Redis는 모든 키가 단일 네임스페이스라 다른 용도의 키와 구분하기 위해 prefix 사용
    private static final String PREFIX = "blacklist:";

    // StringRedisTemplate: 키·값 모두 String인 단순 구조에 최적화된 Redis 클라이언트
    // RedisTemplate<Object, Object>보다 직렬화 설정이 간단하고 오버헤드가 적음
    private final StringRedisTemplate redisTemplate;

    public AccessTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 로그아웃된 토큰을 블랙리스트에 등록한다.
     *
     * TTL = (토큰 만료 시각 - 현재 시각): 토큰이 만료되면 블랙리스트에도 불필요하므로
     * 같은 시점에 Redis 키가 자동 삭제되도록 설정한다.
     * TTL이 0 이하면 이미 만료된 토큰 — 저장하지 않아도 JWT 검증에서 막히므로 무시한다.
     */
    public void blacklist(String token, Instant expiresAt) {
        long ttlSeconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
        }
    }

    /**
     * 블랙리스트 여부를 확인한다.
     * Redis의 EXISTS 명령으로 O(1) 조회 — 토큰이 만료되면 Redis가 키를 자동 삭제하므로
     * 별도의 만료 시각 비교 없이 키 존재 여부만 확인하면 됨.
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
