package com.nextech.moadream.server.v1.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nextech.moadream.server.v1.external.dto.UtilityUsageRequest;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageResponse;

@FeignClient(name = "electricity-api", url = "${external.api.electricity.url:http://localhost:9000}", fallback = ElectricityApiClientFallback.class)
public interface ElectricityApiClient {

    @PostMapping("/api/usage")
    UtilityUsageResponse getUsageData(@RequestBody UtilityUsageRequest request);
}
