package com.nextech.moadream.server.v1.domain.oauth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nextech.moadream.server.v1.domain.oauth.dto.KakaoUserInfoResponse;
import com.nextech.moadream.server.v1.global.config.KakaoOAuthProperties;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final WebClient webClient;

    /**
     * 카카오 Access Token을 검증하고 사용자 정보를 조회합니다.
     *
     * @param accessToken 클라이언트로부터 받은 카카오 Access Token
     * @return 카카오 사용자 정보
     * @throws BusinessException 토큰이 유효하지 않거나 만료된 경우
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(kakaoOAuthProperties.getUserInfoUrl())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            log.error("Invalid or expired Kakao access token", e);
            throw new BusinessException(ErrorCode.OAUTH_INVALID_TOKEN);
        } catch (Exception e) {
            log.error("Failed to get Kakao user info", e);
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED);
        }
    }
}
