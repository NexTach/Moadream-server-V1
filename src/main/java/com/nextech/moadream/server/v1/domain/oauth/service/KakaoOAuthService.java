package com.nextech.moadream.server.v1.domain.oauth.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.nextech.moadream.server.v1.domain.oauth.dto.KakaoTokenResponse;
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

    public KakaoTokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthProperties.getClientId());
        params.add("client_secret", kakaoOAuthProperties.getClientSecret());
        params.add("redirect_uri", kakaoOAuthProperties.getRedirectUri());
        params.add("code", code);

        try {
            return webClient.post().uri(kakaoOAuthProperties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(params).retrieve()
                    .bodyToMono(KakaoTokenResponse.class).block();
        } catch (Exception e) {
            log.error("Failed to get Kakao access token", e);
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
        }
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        try {
            return webClient.get().uri(kakaoOAuthProperties.getUserInfoUrl())
                    .header("Authorization", "Bearer " + accessToken).retrieve().bodyToMono(KakaoUserInfoResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get Kakao user info", e);
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED);
        }
    }

    public String getAuthorizationUrl() {
        return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code", kakaoOAuthProperties.getAuthUrl(),
                kakaoOAuthProperties.getClientId(), kakaoOAuthProperties.getRedirectUri());
    }
}
