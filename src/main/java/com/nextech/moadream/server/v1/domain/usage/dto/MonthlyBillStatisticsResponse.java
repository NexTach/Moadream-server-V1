package com.nextech.moadream.server.v1.domain.usage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "월간 청구서 통계 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyBillStatisticsResponse {

    @Schema(description = "총 청구 금액", example = "150000.00")
    private BigDecimal totalCharge;

    @Schema(description = "평균 청구 금액", example = "50000.00")
    private BigDecimal averageCharge;

    @Schema(description = "최대 청구 금액", example = "70000.00")
    private BigDecimal maxCharge;

    @Schema(description = "최소 청구 금액", example = "30000.00")
    private BigDecimal minCharge;

    @Schema(description = "미납 청구서 수", example = "2")
    private Long unpaidCount;

    @Schema(description = "청구서 목록")
    private List<MonthlyBillResponse> bills;

    public static MonthlyBillStatisticsResponse of(
            BigDecimal totalCharge, BigDecimal averageCharge,
            BigDecimal maxCharge, BigDecimal minCharge,
            Long unpaidCount, List<MonthlyBillResponse> bills) {
        return MonthlyBillStatisticsResponse.builder()
                .totalCharge(totalCharge)
                .averageCharge(averageCharge)
                .maxCharge(maxCharge)
                .minCharge(minCharge)
                .unpaidCount(unpaidCount)
                .bills(bills)
                .build();
    }
}