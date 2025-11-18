package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "가스 청구서 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GasBillRequest {

    @Schema(description = "청구 월", example = "2025-01-01")
    @NotNull(message = "청구 월은 필수입니다.")
    private LocalDate billingMonth;

    @Schema(description = "기본 요금", example = "1200.00")
    @NotNull(message = "기본 요금은 필수입니다.")
    private BigDecimal basicCharge;

    @Schema(description = "취사 요금", example = "3000.00")
    @NotNull(message = "취사 요금은 필수입니다.")
    private BigDecimal cookingCharge;

    @Schema(description = "난방 요금", example = "25000.00")
    @NotNull(message = "난방 요금은 필수입니다.")
    private BigDecimal heatingCharge;

    @Schema(description = "공급가액", example = "27000.00")
    @NotNull(message = "공급가액은 필수입니다.")
    private BigDecimal supplyPrice;

    @Schema(description = "부가세", example = "2700.00")
    @NotNull(message = "부가세는 필수입니다.")
    private BigDecimal vat;

    @Schema(description = "총 사용량 (m³)", example = "45.00")
    @NotNull(message = "총 사용량은 필수입니다.")
    private BigDecimal totalUsage;

    @Schema(description = "납부 기한", example = "2025-02-15")
    private LocalDate dueDate;
}
