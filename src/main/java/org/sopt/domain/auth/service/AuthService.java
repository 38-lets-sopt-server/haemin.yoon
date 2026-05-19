package org.sopt.domain.auth.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.sopt.domain.auth.dto.response.TokenResponse;
import org.sopt.domain.auth.entity.RefreshToken;
import org.sopt.domain.auth.exception.AuthException;
import org.sopt.domain.auth.exception.code.AuthErrorCode;
import org.sopt.domain.auth.repository.RefreshTokenRepository;
import org.sopt.domain.user.entity.User;
import org.sopt.domain.user.repository.UserRepository;
import org.sopt.global.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // 타이밍 공격 방지용 더미 해시
    // 이메일이 존재하지 않는 경우에도 BCrypt 연산을 동일하게 실행해 응답 시간을 일정하게 만듦
    // 서버 시작 시 한 번만 생성되며, 임의값을 해시했으므로 실제 비밀번호와 절대 일치하지 않음
    private final String dummyHash;

    @Value("${security.jwt.refresh-token-expires-in-seconds:1209600}")
    private long refreshTokenExpiresInSeconds;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
    }

    /**
     * 이메일·비밀번호 검증 후 Access Token + Refresh Token을 반환한다.
     *
     * Refresh Token은 DB에 1개만 유지:
     * 로그인할 때마다 기존 토큰을 삭제하고 새로 저장하여
     * 다른 기기·세션에서 발급된 이전 토큰을 자동 무효화한다.
     */
    @Transactional
    public TokenResponse login(String email, String password) {
        // orElse(null) 사용 이유:
        // orElseThrow()를 쓰면 이메일 없을 때 BCrypt를 건너뛰어 응답이 즉시 반환됨.
        // 이메일 있고 비밀번호 틀릴 때는 BCrypt가 ~100ms 걸려 응답 시간 차이로 이메일 존재 여부가 노출됨(타이밍 공격).
        // 이메일 존재 여부와 무관하게 항상 BCrypt를 실행해 응답 시간을 동일하게 만든다.
        User user = userRepository.findByEmail(email).orElse(null);

        String hashToCompare = (user != null) ? user.getPassword() : dummyHash;

        // matches(입력된 평문, DB의 BCrypt 해시)
        // BCrypt 해시 안에 솔트가 내장되어 있어 matches()가 솔트 추출 → 재해시 → 비교를 자동 처리
        // equals()로 비교 x — 솔트 때문에 동일 비밀번호도 해시값이 매번 다름
        // 이메일 없음·비밀번호 불일치 모두 동일한 에러 코드 반환
        // → 어느 값이 틀렸는지 응답만으로 구분 불가
        if (user == null || !passwordEncoder.matches(password, hashToCompare)) {
            throw new AuthException(AuthErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(
                RefreshToken.of(user.getId(), refreshToken, refreshTokenExpiresInSeconds)
        );

        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * Refresh Token으로 Access Token + Refresh Token을 재발급한다 (Rotation 방식).
     *
     * Rotation이란: 재발급 때마다 Refresh Token 자체도 교체하는 전략.
     * 탈취된 Refresh Token이 공격자에 의해 한 번 사용되면 DB 토큰이 바뀌므로,
     * 이후 피해자의 재발급 요청이 실패하여 침해를 감지할 수 있다.
     */
    @Transactional
    public TokenResponse reissue(String rawRefreshToken) {
        // 1단계: JWT 서명·만료 검증 (위변조·만료 → AUTH_INVALID_TOKEN)
        Long userId;
        try {
            userId = jwtService.verifyAndGetUserId(rawRefreshToken);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.AUTH_INVALID_TOKEN);
        }

        // 2단계: DB 조회 — JWT가 유효하더라도 이미 로그아웃하거나 재발급된 토큰이면 거부
        // (JWT는 stateless라 서버가 단독으로 무효화할 수 없어, DB가 서버 측 revocation 역할을 함)
        RefreshToken storedToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        // 3단계: 새 토큰 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_USER_NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        // 4단계: Rotation — 기존 row를 UPDATE하여 DB에는 항상 유저당 토큰 1개만 존재
        storedToken.rotate(newRefreshToken, refreshTokenExpiresInSeconds);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
