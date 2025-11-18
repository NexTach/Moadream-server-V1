package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.usage.entity.GasBill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "가스 청구서 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GasBillResponse {

    @Schema(description = "가스 청구서 ID", example = "1")
    private Long gasBillId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "청구 월", example = "2025-01-01")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "1200.00")
    private BigDecimal basicCharge;

    @Schema(description = "취사 요금", example = "3000.00")
    private BigDecimal cookingCharge;

    @Schema(description = "난방 요금", example = "25000.00")
    private BigDecimal heatingCharge;

    @Schema(description = "공급가액", example = "27000.00")
    private BigDecimal supplyPrice;

    @Schema(description = "부가세", example = "2700.00")
    private BigDecimal vat;

    @Schema(description = "총 요금", example = "58900.00")
    private BigDecimal totalCharge;

    @Schema(description = "총 사용량 (m³)", example = "45.00")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;

    @Schema(description = "납부 여부", example = "false")
    private Boolean isPaid;

    public static GasBillResponse from(GasBill bill) {
        return GasBillResponse.builder().gasBillId(bill.getGasBillId()).userId(bill.getUser().getUserId())
                .billingMonth(bill.getBillingMonth()).basicCharge(bill.getBasicCharge())
                .cookingCharge(bill.getCookingCharge()).heatingCharge(bill.getHeatingCharge())
                .supplyPrice(bill.getSupplyPrice()).vat(bill.getVat()).totalCharge(bill.getTotalCharge())
                .totalUsage(bill.getTotalUsage()).dueDate(bill.getDueDate()).isPaid(bill.getIsPaid()).build();
    }
}
