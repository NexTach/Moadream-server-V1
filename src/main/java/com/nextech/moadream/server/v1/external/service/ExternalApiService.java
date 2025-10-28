package com.nextech.moadream.server.v1.external.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.external.client.ElectricityApiClient;
import com.nextech.moadream.server.v1.external.client.GasApiClient;
import com.nextech.moadream.server.v1.external.client.WaterApiClient;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageRequest;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageResponse;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final ElectricityApiClient electricityApiClient;
    private final WaterApiClient waterApiClient;
    private final GasApiClient gasApiClient;

    public UtilityUsageResponse fetchUsageData(String customerId, UtilityType utilityType, LocalDateTime startDate,
            LocalDateTime endDate) {
        UtilityUsageRequest request = UtilityUsageRequest.builder().customerId(customerId).utilityType(utilityType)
                .startDate(startDate).endDate(endDate).build();
        try {
            return switch (utilityType) {
                case ELECTRICITY -> electricityApiClient.getUsageData(request);
                case WATER -> waterApiClient.getUsageData(request);
                case GAS -> gasApiClient.getUsageData(request);
            };
        } catch (Exception e) {
            log.error("Failed to fetch usage data for customer: {}, utilityType: {}", customerId, utilityType, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public UtilityUsageResponse fetchElectricityUsage(String customerId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return fetchUsageData(customerId, UtilityType.ELECTRICITY, startDate, endDate);
    }

    public UtilityUsageResponse fetchWaterUsage(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return fetchUsageData(customerId, UtilityType.WATER, startDate, endDate);
    }

    public UtilityUsageResponse fetchGasUsage(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return fetchUsageData(customerId, UtilityType.GAS, startDate, endDate);
    }
}
