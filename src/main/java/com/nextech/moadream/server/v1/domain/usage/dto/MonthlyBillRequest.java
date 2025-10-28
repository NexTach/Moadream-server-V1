package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "월간 청구서 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBillRequest {

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    @NotNull(message = "사용량 유형은 필수입니다.")
    private UtilityType utilityType;

    @Schema(description = "청구 월", example = "2025-01-01")
    @NotNull(message = "청구 월은 필수입니다.")
    private LocalDate billingMonth;

    @Schema(description = "총 사용량", example = "350.50")
    @NotNull(message = "총 사용량은 필수입니다.")
    private BigDecimal totalUsage;

    @Schema(description = "총 요금", example = "45000.00")
    @NotNull(message = "총 요금은 필수입니다.")
    private BigDecimal totalCharge;

    @Schema(description = "전월 사용량", example = "320.00")
    private BigDecimal previousMonthUsage;

    @Schema(description = "전월 요금", example = "40000.00")
    private BigDecimal previousMonthCharge;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;
}
