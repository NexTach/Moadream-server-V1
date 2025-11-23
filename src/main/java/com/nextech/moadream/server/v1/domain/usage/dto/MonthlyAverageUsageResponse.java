package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "월별 평균 사용량 응답")
public class MonthlyAverageUsageResponse {

    @Schema(description = "년도 (예: 2025)", example = "2025")
    private Integer year;

    @Schema(description = "월 (1-12)", example = "4")
    private Integer month;

    @Schema(description = "사용량 유형 (ELECTRICITY, GAS, WATER)", example = "ELECTRICITY")
    private UtilityType utilityType;

    @Schema(description = "평균 사용량", example = "6.50")
    private BigDecimal averageUsage;

    @Schema(description = "총 사용량", example = "195.00")
    private BigDecimal totalUsage;

    @Schema(description = "평균 요금", example = "1170.00")
    private BigDecimal averageCharge;

    @Schema(description = "총 요금", example = "35100.00")
    private BigDecimal totalCharge;

    @Schema(description = "데이터 개수 (해당 월의 측정 횟수)", example = "30")
    private Long dataCount;

    @Schema(description = "단위 (kWh, m³ 등)", example = "kWh")
    private String unit;
}
