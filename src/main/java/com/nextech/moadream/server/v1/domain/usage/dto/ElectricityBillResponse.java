package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.usage.entity.ElectricityBill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "전기 청구서 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ElectricityBillResponse {

    @Schema(description = "전기 청구서 ID", example = "1")
    private Long electricityBillId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "청구 월", example = "2025-01-01")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "1000.00")
    private BigDecimal basicCharge;

    @Schema(description = "전력량 요금 (구간별 합계)", example = "15000.00")
    private BigDecimal energyCharge;

    @Schema(description = "기후환경요금", example = "2000.00")
    private BigDecimal climateEnvironmentCharge;

    @Schema(description = "연료비조정요금", example = "500.00")
    private BigDecimal fuelAdjustmentCharge;

    @Schema(description = "부가세", example = "1850.00")
    private BigDecimal vat;

    @Schema(description = "전력산업기반기금", example = "900.00")
    private BigDecimal electricIndustryFund;

    @Schema(description = "총 요금", example = "21250.00")
    private BigDecimal totalCharge;

    @Schema(description = "총 사용량 (kWh)", example = "350.50")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;

    @Schema(description = "납부 여부", example = "false")
    private Boolean isPaid;

    public static ElectricityBillResponse from(ElectricityBill bill) {
        return ElectricityBillResponse.builder().electricityBillId(bill.getElectricityBillId())
                .userId(bill.getUser().getUserId()).billingMonth(bill.getBillingMonth())
                .basicCharge(bill.getBasicCharge()).energyCharge(bill.getEnergyCharge())
                .climateEnvironmentCharge(bill.getClimateEnvironmentCharge())
                .fuelAdjustmentCharge(bill.getFuelAdjustmentCharge()).vat(bill.getVat())
                .electricIndustryFund(bill.getElectricIndustryFund()).totalCharge(bill.getTotalCharge())
                .totalUsage(bill.getTotalUsage()).dueDate(bill.getDueDate()).isPaid(bill.getIsPaid()).build();
    }
}
