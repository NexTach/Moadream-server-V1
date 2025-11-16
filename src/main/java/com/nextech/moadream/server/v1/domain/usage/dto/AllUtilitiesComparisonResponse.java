package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "전체 유형 월간 청구서 비교 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AllUtilitiesComparisonResponse {

    @Schema(description = "비교 기준 월", example = "2024-01-01")
    private LocalDate comparisonMonth;

    @Schema(description = "전체 유형별 비교 데이터")
    private List<MonthlyBillComparisonResponse> comparisons;

    @Schema(description = "전체 요금 합계", example = "150000.00")
    private BigDecimal totalCurrentCharge;

    @Schema(description = "전체 이전 요금 합계", example = "130000.00")
    private BigDecimal totalPreviousCharge;

    @Schema(description = "전체 요금 증감률 (%)", example = "15.38")
    private BigDecimal totalChargeChangeRate;

    @Schema(description = "전체 요금 증감액", example = "20000.00")
    private BigDecimal totalChargeChange;

    public static AllUtilitiesComparisonResponse of(LocalDate comparisonMonth,
            List<MonthlyBillComparisonResponse> comparisons, BigDecimal totalCurrentCharge,
            BigDecimal totalPreviousCharge, BigDecimal totalChargeChangeRate, BigDecimal totalChargeChange) {
        return AllUtilitiesComparisonResponse.builder().comparisonMonth(comparisonMonth).comparisons(comparisons)
                .totalCurrentCharge(totalCurrentCharge).totalPreviousCharge(totalPreviousCharge)
                .totalChargeChangeRate(totalChargeChangeRate).totalChargeChange(totalChargeChange).build();
    }
}

