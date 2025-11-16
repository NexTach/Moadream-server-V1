package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "월간 청구서 전월 대비 비교 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyBillComparisonResponse {

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    private UtilityType utilityType;

    @Schema(description = "현재 월", example = "2024-01-01")
    private LocalDate currentMonth;

    @Schema(description = "이전 월", example = "2023-12-01")
    private LocalDate previousMonth;

    @Schema(description = "현재 월 사용량", example = "350.5")
    private BigDecimal currentUsage;

    @Schema(description = "이전 월 사용량", example = "300.0")
    private BigDecimal previousUsage;

    @Schema(description = "현재 월 요금", example = "45000.00")
    private BigDecimal currentCharge;

    @Schema(description = "이전 월 요금", example = "40000.00")
    private BigDecimal previousCharge;

    @Schema(description = "사용량 증감률 (%)", example = "16.83")
    private BigDecimal usageChangeRate;

    @Schema(description = "요금 증감률 (%)", example = "12.5")
    private BigDecimal chargeChangeRate;

    @Schema(description = "사용량 증감 (절대값)", example = "50.5")
    private BigDecimal usageChange;

    @Schema(description = "요금 증감 (절대값)", example = "5000.00")
    private BigDecimal chargeChange;

    @Schema(description = "증가 여부", example = "true")
    private Boolean isIncrease;

    public static MonthlyBillComparisonResponse of(UtilityType utilityType, LocalDate currentMonth,
            LocalDate previousMonth, BigDecimal currentUsage, BigDecimal previousUsage, BigDecimal currentCharge,
            BigDecimal previousCharge, BigDecimal usageChangeRate, BigDecimal chargeChangeRate,
            BigDecimal usageChange, BigDecimal chargeChange, Boolean isIncrease) {
        return MonthlyBillComparisonResponse.builder().utilityType(utilityType).currentMonth(currentMonth)
                .previousMonth(previousMonth).currentUsage(currentUsage).previousUsage(previousUsage)
                .currentCharge(currentCharge).previousCharge(previousCharge).usageChangeRate(usageChangeRate)
                .chargeChangeRate(chargeChangeRate).usageChange(usageChange).chargeChange(chargeChange)
                .isIncrease(isIncrease).build();
    }
}

