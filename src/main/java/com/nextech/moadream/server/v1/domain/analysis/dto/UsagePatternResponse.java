package com.nextech.moadream.server.v1.domain.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용 패턴 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsagePatternResponse {

    @Schema(description = "패턴 ID")
    private Long patternId;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "유틸리티 타입")
    private UtilityType utilityType;

    @Schema(description = "주기 타입 (일별/주별/월별/계절별)")
    private FrequencyType frequencyType;

    @Schema(description = "평균 사용량")
    private BigDecimal averageUsage;

    @Schema(description = "피크 사용량")
    private BigDecimal peakUsage;

    @Schema(description = "오프피크 사용량")
    private BigDecimal offPeakUsage;

    @Schema(description = "추세 (증가/감소/안정)")
    private String trend;

    @Schema(description = "최종 업데이트 시간")
    private LocalDateTime updatedAt;

    public static UsagePatternResponse from(UsagePattern usagePattern) {
        return UsagePatternResponse.builder().patternId(usagePattern.getPatternId())
                .userId(usagePattern.getUser().getUserId()).utilityType(usagePattern.getUtilityType())
                .frequencyType(usagePattern.getFrequencyType()).averageUsage(usagePattern.getAverageUsage())
                .peakUsage(usagePattern.getPeakUsage()).offPeakUsage(usagePattern.getOffPeakUsage())
                .trend(usagePattern.getTrend()).updatedAt(usagePattern.getUpdatedAt()).build();
    }
}
