package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "수도 청구서 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WaterBillRequest {

    @Schema(description = "청구 월", example = "2025-01-01")
    @NotNull(message = "청구 월은 필수입니다.")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "800.00")
    @NotNull(message = "기본 요금은 필수입니다.")
    private BigDecimal basicCharge;

    @Schema(description = "상수도 요금", example = "5000.00")
    @NotNull(message = "상수도 요금은 필수입니다.")
    private BigDecimal waterSupplyCharge;

    @Schema(description = "하수도 요금", example = "3000.00")
    @NotNull(message = "하수도 요금은 필수입니다.")
    private BigDecimal sewageCharge;

    @Schema(description = "물이용부담금", example = "1000.00")
    @NotNull(message = "물이용부담금은 필수입니다.")
    private BigDecimal waterUsageCharge;

    @Schema(description = "총 사용량 (m³)", example = "25.00")
    @NotNull(message = "총 사용량은 필수입니다.")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;
}
