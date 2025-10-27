package com.nextech.moadream.server.v1.external.client;

import com.nextech.moadream.server.v1.external.dto.UtilityUsageRequest;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "utility-api",
        contextId = "gas-api",
        url = "${external.api.gas.url:http://localhost:9000}",
        fallback = GasApiClientFallback.class
)
public interface GasApiClient {

    @PostMapping("/api/usage")
    UtilityUsageResponse getUsageData(@RequestBody UtilityUsageRequest request);
}