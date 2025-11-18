package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.usage.entity.WaterBill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "수도 청구서 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WaterBillResponse {

    @Schema(description = "수도 청구서 ID", example = "1")
    private Long waterBillId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "청구 월", example = "2025-01-01")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "800.00")
    private BigDecimal basicCharge;

    @Schema(description = "상수도 요금", example = "5000.00")
    private BigDecimal waterSupplyCharge;

    @Schema(description = "하수도 요금", example = "3000.00")
    private BigDecimal sewageCharge;

    @Schema(description = "물이용부담금", example = "1000.00")
    private BigDecimal waterUsageCharge;

    @Schema(description = "총 요금", example = "9800.00")
    private BigDecimal totalCharge;

    @Schema(description = "총 사용량 (m³)", example = "25.00")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;

    @Schema(description = "납부 여부", example = "false")
    private Boolean isPaid;

    public static WaterBillResponse from(WaterBill bill) {
        return WaterBillResponse.builder().waterBillId(bill.getWaterBillId()).userId(bill.getUser().getUserId())
                .billingMonth(bill.getBillingMonth()).basicCharge(bill.getBasicCharge())
                .waterSupplyCharge(bill.getWaterSupplyCharge()).sewageCharge(bill.getSewageCharge())
                .waterUsageCharge(bill.getWaterUsageCharge()).totalCharge(bill.getTotalCharge())
                .totalUsage(bill.getTotalUsage()).dueDate(bill.getDueDate()).isPaid(bill.getIsPaid()).build();
    }
}
