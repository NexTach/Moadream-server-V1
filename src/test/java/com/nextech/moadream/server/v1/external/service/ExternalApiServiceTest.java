package com.nextech.moadream.server.v1.external.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.external.client.ElectricityApiClient;
import com.nextech.moadream.server.v1.external.client.GasApiClient;
import com.nextech.moadream.server.v1.external.client.WaterApiClient;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageRequest;
import com.nextech.moadream.server.v1.external.dto.UtilityUsageResponse;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalApiService 테스트")
class ExternalApiServiceTest {

    @Mock
    private ElectricityApiClient electricityApiClient;

    @Mock
    private WaterApiClient waterApiClient;

    @Mock
    private GasApiClient gasApiClient;

    @InjectMocks
    private ExternalApiService externalApiService;

    private String customerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UtilityUsageResponse mockResponse;

    @BeforeEach
    void setUp() {
        customerId = "CUST12345";
        startDate = LocalDateTime.of(2025, 10, 1, 0, 0);
        endDate = LocalDateTime.of(2025, 10, 31, 23, 59);
        mockResponse = new UtilityUsageResponse();
    }

    @Test
    @DisplayName("전기 사용량 조회 성공")
    void fetchElectricityUsage_Success() {
        // given
        given(electricityApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchElectricityUsage(customerId, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        verify(electricityApiClient).getUsageData(any(UtilityUsageRequest.class));
    }

    @Test
    @DisplayName("수도 사용량 조회 성공")
    void fetchWaterUsage_Success() {
        // given
        given(waterApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchWaterUsage(customerId, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        verify(waterApiClient).getUsageData(any(UtilityUsageRequest.class));
    }

    @Test
    @DisplayName("가스 사용량 조회 성공")
    void fetchGasUsage_Success() {
        // given
        given(gasApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchGasUsage(customerId, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        verify(gasApiClient).getUsageData(any(UtilityUsageRequest.class));
    }

    @Test
    @DisplayName("전기 API 오류 시 예외 발생")
    void fetchElectricityUsage_ApiError() {
        // given
        given(electricityApiClient.getUsageData(any(UtilityUsageRequest.class)))
                .willThrow(new RuntimeException("API Error"));

        // when & then
        assertThatThrownBy(() -> externalApiService.fetchElectricityUsage(customerId, startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
    }

    @Test
    @DisplayName("수도 API 오류 시 예외 발생")
    void fetchWaterUsage_ApiError() {
        // given
        given(waterApiClient.getUsageData(any(UtilityUsageRequest.class)))
                .willThrow(new RuntimeException("API Error"));

        // when & then
        assertThatThrownBy(() -> externalApiService.fetchWaterUsage(customerId, startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
    }

    @Test
    @DisplayName("가스 API 오류 시 예외 발생")
    void fetchGasUsage_ApiError() {
        // given
        given(gasApiClient.getUsageData(any(UtilityUsageRequest.class)))
                .willThrow(new RuntimeException("API Error"));

        // when & then
        assertThatThrownBy(() -> externalApiService.fetchGasUsage(customerId, startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
    }

    @Test
    @DisplayName("유틸리티 타입별 사용량 조회 성공 - ELECTRICITY")
    void fetchUsageData_Electricity_Success() {
        // given
        given(electricityApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchUsageData(customerId, UtilityType.ELECTRICITY, startDate,
                endDate);

        // then
        assertThat(result).isNotNull();
        verify(electricityApiClient).getUsageData(any(UtilityUsageRequest.class));
    }

    @Test
    @DisplayName("유틸리티 타입별 사용량 조회 성공 - WATER")
    void fetchUsageData_Water_Success() {
        // given
        given(waterApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchUsageData(customerId, UtilityType.WATER, startDate,
                endDate);

        // then
        assertThat(result).isNotNull();
        verify(waterApiClient).getUsageData(any(UtilityUsageRequest.class));
    }

    @Test
    @DisplayName("유틸리티 타입별 사용량 조회 성공 - GAS")
    void fetchUsageData_Gas_Success() {
        // given
        given(gasApiClient.getUsageData(any(UtilityUsageRequest.class))).willReturn(mockResponse);

        // when
        UtilityUsageResponse result = externalApiService.fetchUsageData(customerId, UtilityType.GAS, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        verify(gasApiClient).getUsageData(any(UtilityUsageRequest.class));
    }
}