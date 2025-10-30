package com.nextech.moadream.server.v1.domain.usage.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsageDataService 테스트")
class UsageDataServiceTest {

    @Mock
    private UsageDataRepository usageDataRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingRepository userSettingRepository;

    @Mock
    private UsageAlertRepository usageAlertRepository;

    @InjectMocks
    private UsageDataService usageDataService;

    private User testUser;
    private UsageData testUsageData;
    private UsageDataRequest usageDataRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("test@example.com").passwordHash("password").name("테스트").phone("010-1234-5678")
                .address("서울").dateOfBirth("1990-01-01").userVerificationCode("CODE").build();
        org.springframework.test.util.ReflectionTestUtils.setField(testUser, "userId", 1L);

        LocalDateTime measuredAt = LocalDateTime.of(2025, 10, 30, 10, 0);

        testUsageData = UsageData.builder().user(testUser).utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(100.5)).unit("kWh").currentCharge(BigDecimal.valueOf(15000))
                .measuredAt(measuredAt).build();
        org.springframework.test.util.ReflectionTestUtils.setField(testUsageData, "usageId", 1L);

        usageDataRequest = new UsageDataRequest(UtilityType.ELECTRICITY, BigDecimal.valueOf(100.5), "kWh",
                BigDecimal.valueOf(15000), measuredAt);
    }

    @Test
    @DisplayName("사용량 데이터 생성 성공")
    void createUsageData_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.save(any(UsageData.class))).willReturn(testUsageData);
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.empty());

        // when
        UsageDataResponse result = usageDataService.createUsageData(1L, usageDataRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        assertThat(result.getUsageAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.5));
        assertThat(result.getCurrentCharge()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        verify(userRepository).findById(1L);
        verify(usageDataRepository).save(any(UsageData.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 사용량 데이터 생성 실패")
    void createUsageData_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usageDataService.createUsageData(999L, usageDataRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(usageDataRepository, never()).save(any(UsageData.class));
    }

    @Test
    @DisplayName("사용자별 사용량 데이터 조회 성공")
    void getUserUsageData_Success() {
        // given
        List<UsageData> usageDataList = Arrays.asList(testUsageData);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findByUser(any(User.class))).willReturn(usageDataList);

        // when
        List<UsageDataResponse> result = usageDataService.getUserUsageData(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(userRepository).findById(1L);
        verify(usageDataRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("유틸리티 타입별 사용량 데이터 조회 성공")
    void getUserUsageDataByType_Success() {
        // given
        List<UsageData> usageDataList = Arrays.asList(testUsageData);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(usageDataList);

        // when
        List<UsageDataResponse> result = usageDataService.getUserUsageDataByType(1L, UtilityType.ELECTRICITY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(usageDataRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
    }

    @Test
    @DisplayName("날짜 범위별 사용량 데이터 조회 성공")
    void getUserUsageDataByDateRange_Success() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 31, 23, 59);
        List<UsageData> usageDataList = Arrays.asList(testUsageData);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findByUserAndMeasuredAtBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class))).willReturn(usageDataList);

        // when
        List<UsageDataResponse> result = usageDataService.getUserUsageDataByDateRange(1L, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(usageDataRepository).findByUserAndMeasuredAtBetween(testUser, startDate, endDate);
    }

    @Test
    @DisplayName("최신 사용량 데이터 조회 성공")
    void getLatestUsageData_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findLatestByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(testUsageData);

        // when
        UsageDataResponse result = usageDataService.getLatestUsageData(1L, UtilityType.ELECTRICITY);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(usageDataRepository).findLatestByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
    }

    @Test
    @DisplayName("최신 사용량 데이터가 없을 때 조회 실패")
    void getLatestUsageData_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findLatestByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> usageDataService.getLatestUsageData(1L, UtilityType.ELECTRICITY))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USAGE_DATA_NOT_FOUND);
    }

    @Test
    @DisplayName("사용량 데이터 수정 성공")
    void updateUsageData_Success() {
        // given
        UsageDataRequest updateRequest = new UsageDataRequest(UtilityType.ELECTRICITY, BigDecimal.valueOf(120.0), "kWh",
                BigDecimal.valueOf(18000), LocalDateTime.of(2025, 10, 30, 10, 0));

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findById(anyLong())).willReturn(Optional.of(testUsageData));

        // when
        UsageDataResponse result = usageDataService.updateUsageData(1L, 1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(usageDataRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용량 데이터 수정 실패")
    void updateUsageData_UsageDataNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usageDataService.updateUsageData(1L, 999L, usageDataRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USAGE_DATA_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자의 사용량 데이터 수정 실패")
    void updateUsageData_UnauthorizedAccess() {
        // given
        User anotherUser = User.builder().email("another@example.com").passwordHash("password").name("다른사용자")
                .phone("010-9999-9999").address("부산").dateOfBirth("1995-01-01").userVerificationCode("CODE2").build();
        org.springframework.test.util.ReflectionTestUtils.setField(anotherUser, "userId", 2L);

        UsageData anotherUsageData = UsageData.builder().user(anotherUser).utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(50.0)).unit("kWh").currentCharge(BigDecimal.valueOf(5000))
                .measuredAt(LocalDateTime.of(2025, 10, 30, 10, 0)).build();
        org.springframework.test.util.ReflectionTestUtils.setField(anotherUsageData, "usageId", 2L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageDataRepository.findById(anyLong())).willReturn(Optional.of(anotherUsageData));

        // when & then
        assertThatThrownBy(() -> usageDataService.updateUsageData(1L, 2L, usageDataRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USAGE_DATA_NOT_FOUND);
    }
}
