package com.nextech.moadream.server.v1.domain.user.dto;

import java.math.BigDecimal;

import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "사용자 설정 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSettingResponse {

    @Schema(description = "설정 ID", example = "1")
    private Long settingId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "월간 예산", example = "200000.00")
    private BigDecimal monthlyBudget;

    @Schema(description = "알림 임계값 (%)", example = "80.00")
    private BigDecimal alertThreshold;

    @Schema(description = "푸시 알림 활성화", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "이메일 알림 활성화", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "효율성 점수", example = "85.50")
    private BigDecimal efficiencyScore;

    public static UserSettingResponse from(UserSetting setting) {
        return UserSettingResponse.builder().settingId(setting.getSettingId()).userId(setting.getUser().getUserId())
                .monthlyBudget(setting.getMonthlyBudget()).alertThreshold(setting.getAlertThreshold())
                .pushEnabled(setting.getPushEnabled()).emailEnabled(setting.getEmailEnabled())
                .efficiencyScore(setting.getEfficiencyScore()).build();
    }
}
