package com.nextech.moadream.server.v1.external.client;

import com.nextech.moadream.server.v1.external.dto.UtilityUsageRequest;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class ElectricityApiClientFallback implements ElectricityApiClient {

    @Override
    public UtilityUsageResponse getUsageData(UtilityUsageRequest request) {
        log.error("Electricity API fallback triggered for customer: {}", request.getCustomerId());
        return UtilityUsageResponse.builder().customerId(request.getCustomerId()).utilityType(request.getUtilityType())
                .records(Collections.emptyList()).status("FAILED").message("전기 사용량 조회 서비스가 일시적으로 사용 불가능합니다.").build();
    }
}
