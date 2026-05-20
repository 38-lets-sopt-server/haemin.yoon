package org.sopt.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

// Google token endpoint가 반환하는 응답 중 우리가 필요한 필드만 매핑
// Google은 snake_case를 사용하므로 @JsonProperty로 매핑
public record GoogleTokenResponse(
        @JsonProperty("access_token") String accessToken
) {}
