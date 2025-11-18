package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "전기 청구서 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityBillRequest {

    @Schema(description = "청구 월", example = "2025-01-01")
    @NotNull(message = "청구 월은 필수입니다.")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "1000.00")
    @NotNull(message = "기본 요금은 필수입니다.")
    private BigDecimal basicCharge;

    @Schema(description = "전력량 요금 (구간별 합계)", example = "15000.00")
    @NotNull(message = "전력량 요금은 필수입니다.")
    private BigDecimal energyCharge;

    @Schema(description = "기후환경요금", example = "2000.00")
    @NotNull(message = "기후환경요금은 필수입니다.")
    private BigDecimal climateEnvironmentCharge;

    @Schema(description = "연료비조정요금", example = "500.00")
    @NotNull(message = "연료비조정요금은 필수입니다.")
    private BigDecimal fuelAdjustmentCharge;

    @Schema(description = "부가세", example = "1850.00")
    @NotNull(message = "부가세는 필수입니다.")
    private BigDecimal vat;

    @Schema(description = "전력산업기반기금", example = "900.00")
    @NotNull(message = "전력산업기반기금은 필수입니다.")
    private BigDecimal electricIndustryFund;

    @Schema(description = "총 사용량 (kWh)", example = "350.50")
    @NotNull(message = "총 사용량은 필수입니다.")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;
}
