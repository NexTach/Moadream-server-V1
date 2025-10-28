package com.nextech.moadream.server.v1.domain.usage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용량 데이터 등록 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UsageDataRequest {

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    @NotNull(message = "사용량 유형은 필수입니다.")
    private UtilityType utilityType;

    @Schema(description = "사용량", example = "123.45")
    @NotNull(message = "사용량은 필수입니다.")
    @Positive(message = "사용량은 0보다 커야 합니다.")
    private BigDecimal usageAmount;

    @Schema(description = "단위", example = "kWh")
    @NotNull(message = "단위는 필수입니다.")
    private String unit;

    @Schema(description = "현재 요금", example = "15000.00")
    private BigDecimal currentCharge;

    @Schema(description = "측정 시간", example = "2025-01-27T10:30:00")
    @NotNull(message = "측정 시간은 필수입니다.")
    private LocalDateTime measuredAt;
}
