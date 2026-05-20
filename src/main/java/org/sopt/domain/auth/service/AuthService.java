package org.sopt.domain.auth.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Instant;
import org.sopt.domain.auth.client.GoogleOAuthClient;
import org.sopt.domain.auth.dto.response.GoogleUserInfoResponse;
import org.sopt.domain.auth.dto.response.TokenResponse;
import org.sopt.domain.auth.entity.RefreshToken;
import org.sopt.domain.auth.exception.AuthException;
import org.sopt.domain.auth.exception.code.AuthErrorCode;
import org.sopt.domain.auth.repository.RefreshTokenRepository;
import org.sopt.domain.user.entity.AuthProvider;
import org.sopt.domain.user.entity.User;
import org.sopt.domain.user.repository.UserRepository;
import org.sopt.global.jwt.AccessTokenBlacklistService;
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
    private final AccessTokenBlacklistService blacklistService;
    private final GoogleOAuthClient googleOAuthClient;

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
            PasswordEncoder passwordEncoder,
            AccessTokenBlacklistService blacklistService,
            GoogleOAuthClient googleOAuthClient
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.blacklistService = blacklistService;
        this.googleOAuthClient = googleOAuthClient;
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

        // OAuth 유저(password == null)도 dummyHash로 처리해 항상 BCrypt를 실행
        // → 타이밍 공격 방지 유지, OAuth 유저의 이메일/비밀번호 로그인 시도 차단
        String hashToCompare = (user != null && user.getPassword() != null) ? user.getPassword() : dummyHash;

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
     * 로그아웃: Refresh Token을 DB에서 삭제하고, Access Token을 블랙리스트에 등록한다.
     *
     * ── Refresh Token 삭제 ──────────────────────────────────────────────────
     * DB에서 해당 유저의 Refresh Token을 제거해 재발급 경로를 차단한다.
     * Refresh Token이 없더라도 에러를 던지지 않음 — 이미 로그아웃된 상태를 멱등(idempotent)하게 처리.
     *
     * ── Access Token 블랙리스트 ─────────────────────────────────────────────
     * JWT는 stateless라 서버가 단독으로 무효화할 수 없다.
     * 만료 전 토큰을 무력화하려면 서버 측에서 사용 금지 목록을 관리해야 한다.
     * JwtAuthFilter가 매 요청마다 블랙리스트를 확인해 등록된 토큰을 차단한다.
     *
     * ── 클라이언트가 401을 감지해 로그인 페이지로 이동하는 흐름 ───────────────
     * 1. 클라이언트가 API를 호출한다 (Access Token을 Authorization 헤더에 포함).
     * 2. 서버가 401 (AUTH4010) 을 반환한다.
     *    - Access Token 만료: JWTVerificationException → SecurityContext 미설정 → 401
     *    - 블랙리스트 등록된 토큰: JwtAuthFilter에서 SecurityContext 미설정 → 401
     * 3. 클라이언트는 401을 받으면 저장된 Refresh Token으로 /api/v1/auth/reissue를 호출한다.
     * 4-a. 재발급 성공(200): 새 Access/Refresh Token을 저장하고 원래 요청을 재시도한다.
     * 4-b. 재발급 실패(401): Refresh Token도 만료되었거나 로그아웃으로 삭제된 경우.
     *      저장된 토큰을 모두 제거하고 로그인 페이지로 이동시킨다.
     */
    @Transactional
    public void logout(Long userId, String accessToken) {
        // Refresh Token 삭제 — 이미 없어도 예외 없이 정상 처리 (멱등성 보장)
        refreshTokenRepository.deleteByUserId(userId);

        // Access Token 블랙리스트 등록
        // getExpiry()는 JWT 서명을 다시 검증하므로 위변조된 토큰을 그대로 등록하는 위험이 없음
        // 만료 시각을 함께 저장해 두면 스케줄러가 만료된 항목을 주기적으로 정리할 수 있음
        Instant expiresAt = jwtService.getExpiry(accessToken);
        blacklistService.blacklist(accessToken, expiresAt);
    }

    /**
     * Google OAuth 2.0 소셜 로그인.
     *
     * ── 흐름 ────────────────────────────────────────────────────────────────
     * 1. 클라이언트로부터 받은 authorization code로 Google access token 교환
     * 2. Google access token으로 유저 정보(이메일, 이름) 조회
     * 3. 이메일로 기존 유저 조회 — 없으면 자동 회원가입(신규 유저 생성)
     * 4. 우리 서버의 JWT 발급 후 반환
     *
     * ── 신규 vs 기존 유저 판별 ────────────────────────────────────────────
     * 이메일을 기준으로 판별한다. 동일 이메일로 LOCAL 회원가입한 유저가 Google 로그인을
     * 시도하면 같은 계정으로 로그인된다 (이메일 기반 계정 병합).
     */
    public String getGoogleAuthorizationUrl() {
        return googleOAuthClient.buildAuthorizationUrl();
    }

    @Transactional
    public TokenResponse googleLogin(String code, String redirectUri) {
        // 1단계: authorization code → Google access token
        String googleAccessToken = googleOAuthClient.getAccessToken(code, redirectUri);

        // 2단계: Google access token → 유저 정보
        GoogleUserInfoResponse userInfo = googleOAuthClient.getUserInfo(googleAccessToken);

        // 3단계: 이메일로 기존 유저 조회, 없으면 신규 생성
        // orElseGet: 유저가 없을 때만 람다 실행 — save 쿼리가 불필요하게 실행되지 않음
        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(
                        User.ofOAuth(userInfo.name(), userInfo.email(), AuthProvider.GOOGLE)
                ));

        // 4단계: JWT 발급 (로그인과 동일한 로직)
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
