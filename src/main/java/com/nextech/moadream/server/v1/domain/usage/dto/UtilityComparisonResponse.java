package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "전기/수도/가스 통합 비교 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilityComparisonResponse {

    @Schema(description = "비교 기준 월", example = "2025-01-01")
    private LocalDate currentMonth;

    @Schema(description = "전기비 증감률 (%)", example = "15.5")
    private BigDecimal electricityChangeRate;

    @Schema(description = "수도비 증감률 (%)", example = "-5.2")
    private BigDecimal waterChangeRate;

    @Schema(description = "가스비 증감률 (%)", example = "25.3")
    private BigDecimal gasChangeRate;

    @Schema(description = "전기비 현재 월 요금", example = "45000.00")
    private BigDecimal electricityCurrentCharge;

    @Schema(description = "수도비 현재 월 요금", example = "15000.00")
    private BigDecimal waterCurrentCharge;

    @Schema(description = "가스비 현재 월 요금", example = "85000.00")
    private BigDecimal gasCurrentCharge;

    @Schema(description = "전기비 이전 월 요금", example = "39000.00")
    private BigDecimal electricityPreviousCharge;

    @Schema(description = "수도비 이전 월 요금", example = "15800.00")
    private BigDecimal waterPreviousCharge;

    @Schema(description = "가스비 이전 월 요금", example = "67850.00")
    private BigDecimal gasPreviousCharge;

    @Schema(description = "총 현재 월 요금", example = "145000.00")
    private BigDecimal totalCurrentCharge;

    @Schema(description = "총 이전 월 요금", example = "122650.00")
    private BigDecimal totalPreviousCharge;

    @Schema(description = "전체 증감률 (%)", example = "18.2")
    private BigDecimal totalChangeRate;

    public static UtilityComparisonResponse of(LocalDate currentMonth, BigDecimal electricityChangeRate,
            BigDecimal waterChangeRate, BigDecimal gasChangeRate, BigDecimal electricityCurrentCharge,
            BigDecimal waterCurrentCharge, BigDecimal gasCurrentCharge, BigDecimal electricityPreviousCharge,
            BigDecimal waterPreviousCharge, BigDecimal gasPreviousCharge, BigDecimal totalCurrentCharge,
            BigDecimal totalPreviousCharge, BigDecimal totalChangeRate) {
        return UtilityComparisonResponse.builder().currentMonth(currentMonth)
                .electricityChangeRate(electricityChangeRate).waterChangeRate(waterChangeRate)
                .gasChangeRate(gasChangeRate).electricityCurrentCharge(electricityCurrentCharge)
                .waterCurrentCharge(waterCurrentCharge).gasCurrentCharge(gasCurrentCharge)
                .electricityPreviousCharge(electricityPreviousCharge).waterPreviousCharge(waterPreviousCharge)
                .gasPreviousCharge(gasPreviousCharge).totalCurrentCharge(totalCurrentCharge)
                .totalPreviousCharge(totalPreviousCharge).totalChangeRate(totalChangeRate).build();
    }
}
