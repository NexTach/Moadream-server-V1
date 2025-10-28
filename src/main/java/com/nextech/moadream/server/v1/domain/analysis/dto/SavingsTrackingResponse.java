package com.nextech.moadream.server.v1.domain.analysis.dto;

import com.nextech.moadream.server.v1.domain.analysis.entity.SavingsTracking;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "절감 효과 추적 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsTrackingResponse {

    @Schema(description = "추적 ID")
    private Long trackingId;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "추천 ID")
    private Long recId;

    @Schema(description = "유틸리티 타입")
    private UtilityType utilityType;

    @Schema(description = "추적 월")
    private LocalDate trackingMonth;

    @Schema(description = "실제 사용량")
    private BigDecimal actualUsage;

    @Schema(description = "기준 비용")
    private BigDecimal baselineCost;

    @Schema(description = "실제 비용")
    private BigDecimal actualCost;

    @Schema(description = "달성한 절감액")
    private BigDecimal savingsAchieved;

    public static SavingsTrackingResponse from(SavingsTracking savingsTracking) {
        return SavingsTrackingResponse.builder()
                .trackingId(savingsTracking.getTrackingId())
                .userId(savingsTracking.getUser().getUserId())
                .recId(savingsTracking.getRecommendation() != null ?
                        savingsTracking.getRecommendation().getRecId() : null)
                .utilityType(savingsTracking.getUtilityType())
                .trackingMonth(savingsTracking.getTrackingMonth())
                .actualUsage(savingsTracking.getActualUsage())
                .baselineCost(savingsTracking.getBaselineCost())
                .actualCost(savingsTracking.getActualCost())
                .savingsAchieved(savingsTracking.getSavingsAchieved())
                .build();
    }
}