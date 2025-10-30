package com.nextech.moadream.server.v1.domain.analysis.service;

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
import org.springframework.test.util.ReflectionTestUtils;

import com.nextech.moadream.server.v1.domain.analysis.dto.UsagePatternResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsagePatternService 테스트")
class UsagePatternServiceTest {

    @Mock
    private UsagePatternRepository usagePatternRepository;

    @Mock
    private UsageDataRepository usageDataRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsagePatternService usagePatternService;

    private User testUser;
    private UsagePattern testPattern;
    private List<UsageData> testUsageDataList;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("password")
                .name("테스트")
                .phone("010-1234-5678")
                .address("서울")
                .dateOfBirth("1990-01-01")
                .userVerificationCode("CODE")
                .build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        testPattern = UsagePattern.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .frequencyType(FrequencyType.MONTHLY)
                .averageUsage(BigDecimal.valueOf(250.50))
                .peakUsage(BigDecimal.valueOf(350.00))
                .offPeakUsage(BigDecimal.valueOf(150.00))
                .trend("안정")
                .build();
        ReflectionTestUtils.setField(testPattern, "patternId", 1L);

        UsageData data1 = UsageData.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(200))
                .unit("kWh")
                .currentCharge(BigDecimal.valueOf(30000))
                .measuredAt(LocalDateTime.now().minusDays(10))
                .build();

        UsageData data2 = UsageData.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(300))
                .unit("kWh")
                .currentCharge(BigDecimal.valueOf(45000))
                .measuredAt(LocalDateTime.now().minusDays(5))
                .build();

        testUsageDataList = Arrays.asList(data1, data2);
    }

    @Test
    @DisplayName("사용자의 패턴 조회 성공")
    void getUserPatterns_Success() {
        // given
        List<UsagePattern> patterns = Arrays.asList(testPattern);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usagePatternRepository.findByUser(any(User.class))).willReturn(patterns);

        // when
        List<UsagePatternResponse> result = usagePatternService.getUserPatterns(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(usagePatternRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("유틸리티 타입별 패턴 조회 성공")
    void getUserPatternsByType_Success() {
        // given
        List<UsagePattern> patterns = Arrays.asList(testPattern);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usagePatternRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(patterns);

        // when
        List<UsagePatternResponse> result = usagePatternService.getUserPatternsByType(1L, UtilityType.ELECTRICITY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(usagePatternRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 패턴 조회 실패")
    void getUserPatterns_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usagePatternService.getUserPatterns(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(usagePatternRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("패턴 분석 - 새 패턴 생성")
    void analyzePattern_CreateNew() {
        // given
        given(usageDataRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(testUsageDataList);
        given(usagePatternRepository.findByUserAndUtilityTypeAndFrequencyType(
                any(User.class), any(UtilityType.class), any(FrequencyType.class)))
                .willReturn(Optional.empty());
        given(usagePatternRepository.save(any(UsagePattern.class))).willReturn(testPattern);

        // when
        UsagePatternResponse result = usagePatternService.analyzePattern(testUser, UtilityType.ELECTRICITY,
                FrequencyType.MONTHLY);

        // then
        assertThat(result).isNotNull();
        verify(usageDataRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
        verify(usagePatternRepository).save(any(UsagePattern.class));
    }

    @Test
    @DisplayName("패턴 분석 - 기존 패턴 업데이트")
    void analyzePattern_UpdateExisting() {
        // given
        given(usageDataRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(testUsageDataList);
        given(usagePatternRepository.findByUserAndUtilityTypeAndFrequencyType(
                any(User.class), any(UtilityType.class), any(FrequencyType.class)))
                .willReturn(Optional.of(testPattern));

        // when
        UsagePatternResponse result = usagePatternService.analyzePattern(testUser, UtilityType.ELECTRICITY,
                FrequencyType.MONTHLY);

        // then
        assertThat(result).isNotNull();
        verify(usageDataRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
        verify(usagePatternRepository, never()).save(any(UsagePattern.class));
    }
}