package com.nextech.moadream.server.v1.domain.chat.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RegionalRateService {

    private static final Map<String, Map<UtilityType, BigDecimal>> REGIONAL_RATES = new HashMap<>();

    static {
        // 서울 요금
        Map<UtilityType, BigDecimal> seoulRates = new HashMap<>();
        seoulRates.put(UtilityType.ELECTRICITY, new BigDecimal("120.0")); // 원/kWh (평균)
        seoulRates.put(UtilityType.WATER, new BigDecimal("580.0")); // 원/㎥
        seoulRates.put(UtilityType.GAS, new BigDecimal("650.0")); // 원/㎥
        REGIONAL_RATES.put("서울", seoulRates);

        // 경기 요금
        Map<UtilityType, BigDecimal> gyeonggiRates = new HashMap<>();
        gyeonggiRates.put(UtilityType.ELECTRICITY, new BigDecimal("115.0"));
        gyeonggiRates.put(UtilityType.WATER, new BigDecimal("550.0"));
        gyeonggiRates.put(UtilityType.GAS, new BigDecimal("620.0"));
        REGIONAL_RATES.put("경기", gyeonggiRates);

        // 인천 요금
        Map<UtilityType, BigDecimal> incheonRates = new HashMap<>();
        incheonRates.put(UtilityType.ELECTRICITY, new BigDecimal("118.0"));
        incheonRates.put(UtilityType.WATER, new BigDecimal("570.0"));
        incheonRates.put(UtilityType.GAS, new BigDecimal("640.0"));
        REGIONAL_RATES.put("인천", incheonRates);

        // 부산 요금
        Map<UtilityType, BigDecimal> busanRates = new HashMap<>();
        busanRates.put(UtilityType.ELECTRICITY, new BigDecimal("110.0"));
        busanRates.put(UtilityType.WATER, new BigDecimal("520.0"));
        busanRates.put(UtilityType.GAS, new BigDecimal("600.0"));
        REGIONAL_RATES.put("부산", busanRates);

        // 대구 요금
        Map<UtilityType, BigDecimal> daeguRates = new HashMap<>();
        daeguRates.put(UtilityType.ELECTRICITY, new BigDecimal("112.0"));
        daeguRates.put(UtilityType.WATER, new BigDecimal("530.0"));
        daeguRates.put(UtilityType.GAS, new BigDecimal("610.0"));
        REGIONAL_RATES.put("대구", daeguRates);

        // 광주 요금
        Map<UtilityType, BigDecimal> gwangjuRates = new HashMap<>();
        gwangjuRates.put(UtilityType.ELECTRICITY, new BigDecimal("108.0"));
        gwangjuRates.put(UtilityType.WATER, new BigDecimal("510.0"));
        gwangjuRates.put(UtilityType.GAS, new BigDecimal("590.0"));
        REGIONAL_RATES.put("광주", gwangjuRates);

        // 대전 요금
        Map<UtilityType, BigDecimal> daejeonRates = new HashMap<>();
        daejeonRates.put(UtilityType.ELECTRICITY, new BigDecimal("111.0"));
        daejeonRates.put(UtilityType.WATER, new BigDecimal("525.0"));
        daejeonRates.put(UtilityType.GAS, new BigDecimal("605.0"));
        REGIONAL_RATES.put("대전", daejeonRates);

        // 울산 요금
        Map<UtilityType, BigDecimal> ulsanRates = new HashMap<>();
        ulsanRates.put(UtilityType.ELECTRICITY, new BigDecimal("109.0"));
        ulsanRates.put(UtilityType.WATER, new BigDecimal("515.0"));
        ulsanRates.put(UtilityType.GAS, new BigDecimal("595.0"));
        REGIONAL_RATES.put("울산", ulsanRates);
    }

    public String extractRegion(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        for (String region : REGIONAL_RATES.keySet()) {
            if (address.contains(region)) {
                return region;
            }
        }

        return "서울"; // 기본값
    }

    public BigDecimal getRegionalRate(String region, UtilityType utilityType) {
        Map<UtilityType, BigDecimal> rates = REGIONAL_RATES.getOrDefault(region, REGIONAL_RATES.get("서울"));
        return rates.getOrDefault(utilityType, BigDecimal.ZERO);
    }

    public BigDecimal calculateEstimatedCost(String region, UtilityType utilityType, BigDecimal usage) {
        BigDecimal rate = getRegionalRate(region, utilityType);
        return usage.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    public String getRegionalRateInfo(String region) {
        Map<UtilityType, BigDecimal> rates = REGIONAL_RATES.getOrDefault(region, REGIONAL_RATES.get("서울"));

        StringBuilder info = new StringBuilder();
        info.append(region).append(" 지역 요금 정보:\n");
        info.append("- 전기: ").append(rates.get(UtilityType.ELECTRICITY)).append("원/kWh\n");
        info.append("- 수도: ").append(rates.get(UtilityType.WATER)).append("원/㎥\n");
        info.append("- 가스: ").append(rates.get(UtilityType.GAS)).append("원/㎥\n");

        return info.toString();
    }

    public String getAllRegionalRates() {
        StringBuilder info = new StringBuilder();
        info.append("전국 주요 지역별 요금 정보:\n\n");

        for (Map.Entry<String, Map<UtilityType, BigDecimal>> entry : REGIONAL_RATES.entrySet()) {
            String region = entry.getKey();
            Map<UtilityType, BigDecimal> rates = entry.getValue();

            info.append("【").append(region).append("】\n");
            info.append("  전기: ").append(rates.get(UtilityType.ELECTRICITY)).append("원/kWh\n");
            info.append("  수도: ").append(rates.get(UtilityType.WATER)).append("원/㎥\n");
            info.append("  가스: ").append(rates.get(UtilityType.GAS)).append("원/㎥\n\n");
        }

        return info.toString();
    }
}