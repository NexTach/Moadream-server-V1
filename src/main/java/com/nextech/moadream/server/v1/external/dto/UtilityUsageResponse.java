package com.nextech.moadream.server.v1.external.dto;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityUsageResponse {
    private String customerId;
    private UtilityType utilityType;
    private List<UsageRecord> records;
    private String status;
    private String message;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageRecord {
        private BigDecimal usageAmount;
        private String unit;
        private BigDecimal charge;
        private LocalDateTime measuredAt;
    }
}