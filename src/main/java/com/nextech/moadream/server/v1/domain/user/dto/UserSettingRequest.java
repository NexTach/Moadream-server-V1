package com.nextech.moadream.server.v1.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "사용자 설정 수정 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserSettingRequest {

    @Schema(description = "월간 예산", example = "200000.00")
    private BigDecimal monthlyBudget;

    @Schema(description = "알림 임계값 (%)", example = "80.00")
    private BigDecimal alertThreshold;

    @Schema(description = "푸시 알림 활성화", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "이메일 알림 활성화", example = "true")
    private Boolean emailEnabled;
}