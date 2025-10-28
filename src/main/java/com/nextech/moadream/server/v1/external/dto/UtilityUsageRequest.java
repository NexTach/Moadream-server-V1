package com.nextech.moadream.server.v1.external.dto;

import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityUsageRequest {
    private String customerId;
    private UtilityType utilityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
