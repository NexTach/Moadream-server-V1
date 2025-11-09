package com.nextech.moadream.server.v1.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "oauth.kakao")
@Getter
@Setter
public class KakaoOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authUrl;
    private String tokenUrl;
    private String userInfoUrl;
}
