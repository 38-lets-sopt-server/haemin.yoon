package org.sopt.domain.auth.dto.response;

// Google userinfo endpoint가 반환하는 응답 중 필요한 필드만 매핑
// id: Google 계정 고유 식별자 (providerId로 활용 가능)
// email, name: 유저 생성에 사용
public record GoogleUserInfoResponse(
        String id,
        String email,
        String name
) {}
