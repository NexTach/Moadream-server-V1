package com.nextech.moadream.server.v1.domain.usage.dto;

import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "사용량 데이터 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UsageDataResponse {

    @Schema(description = "사용량 ID", example = "1")
    private Long usageId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    private UtilityType utilityType;

    @Schema(description = "사용량", example = "123.45")
    private BigDecimal usageAmount;

    @Schema(description = "단위", example = "kWh")
    private String unit;

    @Schema(description = "현재 요금", example = "15000.00")
    private BigDecimal currentCharge;

    @Schema(description = "측정 시간", example = "2025-01-27T10:30:00")
    private LocalDateTime measuredAt;

    @Schema(description = "생성 시간", example = "2025-01-27T10:35:00")
    private LocalDateTime createdAt;

    public static UsageDataResponse from(UsageData usageData) {
        return UsageDataResponse.builder()
                .usageId(usageData.getUsageId())
                .userId(usageData.getUser().getUserId())
                .utilityType(usageData.getUtilityType())
                .usageAmount(usageData.getUsageAmount())
                .unit(usageData.getUnit())
                .currentCharge(usageData.getCurrentCharge())
                .measuredAt(usageData.getMeasuredAt())
                .createdAt(usageData.getCreatedAt())
                .build();
    }
}