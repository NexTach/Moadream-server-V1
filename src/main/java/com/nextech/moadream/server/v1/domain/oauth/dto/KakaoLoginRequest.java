package com.nextech.moadream.server.v1.domain.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;
}
