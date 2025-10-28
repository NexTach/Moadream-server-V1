package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "월간 청구서 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyBillResponse {

    @Schema(description = "청구서 ID", example = "1")
    private Long billId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    private UtilityType utilityType;

    @Schema(description = "청구 월", example = "2025-01-01")
    private LocalDate billingMonth;

    @Schema(description = "총 사용량", example = "350.50")
    private BigDecimal totalUsage;

    @Schema(description = "총 요금", example = "45000.00")
    private BigDecimal totalCharge;

    @Schema(description = "전월 사용량", example = "320.00")
    private BigDecimal previousMonthUsage;

    @Schema(description = "전월 요금", example = "40000.00")
    private BigDecimal previousMonthCharge;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;

    @Schema(description = "납부 여부", example = "false")
    private Boolean isPaid;

    public static MonthlyBillResponse from(MonthlyBill bill) {
        return MonthlyBillResponse.builder().billId(bill.getBillId()).userId(bill.getUser().getUserId())
                .utilityType(bill.getUtilityType()).billingMonth(bill.getBillingMonth())
                .totalUsage(bill.getTotalUsage()).totalCharge(bill.getTotalCharge())
                .previousMonthUsage(bill.getPreviousMonthUsage()).previousMonthCharge(bill.getPreviousMonthCharge())
                .dueDate(bill.getDueDate()).isPaid(bill.getIsPaid()).build();
    }
}
