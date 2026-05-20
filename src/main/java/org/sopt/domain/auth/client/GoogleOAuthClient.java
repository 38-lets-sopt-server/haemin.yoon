package org.sopt.domain.auth.client;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopt.domain.auth.dto.response.GoogleTokenResponse;
import org.sopt.domain.auth.dto.response.GoogleUserInfoResponse;
import org.sopt.domain.auth.exception.AuthException;
import org.sopt.domain.auth.exception.code.AuthErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Google OAuth 2.0 API 호출을 담당하는 클라이언트.
 *
 * ── Authorization Code Flow ─────────────────────────────────────────────
 * 1. 클라이언트가 Google 인가 화면에서 authorization code를 받는다.
 * 2. 클라이언트가 code를 우리 서버에 전달한다.
 * 3. 서버(이 클래스)가 code로 Google token endpoint에서 access token을 교환한다.
 * 4. 서버가 access token으로 Google userinfo endpoint에서 유저 정보를 조회한다.
 *
 * ── RestClient 사용 이유 ─────────────────────────────────────────────────
 * Spring 6.1(Boot 3.2)에서 도입된 동기 HTTP 클라이언트.
 * WebClient보다 간결하고 RestTemplate보다 현대적인 API를 제공함.
 */
@Component
public class GoogleOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthClient.class);

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String authorizationUri;
    private final String tokenUri;
    private final String userInfoUri;
    private final String callbackUri;

    public GoogleOAuthClient(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.authorization-uri}") String authorizationUri,
            @Value("${oauth.google.token-uri}") String tokenUri,
            @Value("${oauth.google.user-info-uri}") String userInfoUri,
            @Value("${oauth.google.callback-uri}") String callbackUri
    ) {
        // RestClient.create()로 기본 설정의 RestClient 생성
        // 빈으로 주입받지 않고 직접 생성하는 이유: GoogleOAuthClient 전용이라 공유할 필요 없음
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizationUri = authorizationUri;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
        this.callbackUri = callbackUri;
    }

    /**
     * Google 인가 화면 URL을 생성한다.
     * client_id 등 민감한 값이 서버에서 조립되므로 프론트에 노출되지 않음.
     */
    public String buildAuthorizationUrl() {
        return authorizationUri
                + "?client_id=" + clientId
                + "&redirect_uri=" + callbackUri
                + "&response_type=code"
                + "&scope=email%20profile";
    }

    /**
     * authorization code를 Google access token으로 교환한다.
     *
     * Google token endpoint는 application/x-www-form-urlencoded 형식을 요구함.
     * redirect_uri는 code 발급 시 사용한 값과 정확히 일치해야 Google이 검증을 통과시킴.
     */
    public String getAccessToken(String code, String redirectUri) {
        // form 파라미터를 MultiValueMap으로 구성 — RestClient가 자동으로 form-urlencoded로 직렬화
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        GoogleTokenResponse response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                // Google이 4xx/5xx를 반환하면 (잘못된 code, 만료된 code 등) 예외로 변환
                .onStatus(status -> status.isError(), (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    log.error("Google token 교환 실패 — status: {}, body: {}", res.getStatusCode(), body);
                    throw new AuthException(AuthErrorCode.AUTH_OAUTH_PROVIDER_ERROR);
                })
                .body(GoogleTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new AuthException(AuthErrorCode.AUTH_OAUTH_PROVIDER_ERROR);
        }

        return response.accessToken();
    }

    /**
     * Google access token으로 유저 정보(이메일, 이름)를 조회한다.
     */
    public GoogleUserInfoResponse getUserInfo(String googleAccessToken) {
        GoogleUserInfoResponse response = restClient.get()
                .uri(userInfoUri)
                // Google userinfo API는 Bearer 토큰 인증을 요구함
                .header("Authorization", "Bearer " + googleAccessToken)
                .retrieve()
                .onStatus(status -> status.isError(), (req, res) -> {
                    throw new AuthException(AuthErrorCode.AUTH_OAUTH_PROVIDER_ERROR);
                })
                .body(GoogleUserInfoResponse.class);

        if (response == null) {
            throw new AuthException(AuthErrorCode.AUTH_OAUTH_PROVIDER_ERROR);
        }

        return response;
    }
}
