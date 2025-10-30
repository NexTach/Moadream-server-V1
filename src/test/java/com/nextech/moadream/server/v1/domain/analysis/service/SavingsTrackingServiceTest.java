package com.nextech.moadream.server.v1.domain.analysis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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

import com.nextech.moadream.server.v1.domain.analysis.dto.SavingsTrackingResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.entity.SavingsTracking;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.SavingsTrackingRepository;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsTrackingService 테스트")
class SavingsTrackingServiceTest {

    @Mock
    private SavingsTrackingRepository savingsTrackingRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private UsageDataRepository usageDataRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SavingsTrackingService savingsTrackingService;

    private User testUser;
    private Recommendation testRecommendation;
    private SavingsTracking testTracking;

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

        testRecommendation = Recommendation.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .isApplied(false)
                .build();
        ReflectionTestUtils.setField(testRecommendation, "recId", 1L);

        testTracking = SavingsTracking.builder()
                .user(testUser)
                .recommendation(testRecommendation)
                .utilityType(UtilityType.ELECTRICITY)
                .trackingMonth(LocalDate.of(2025, 10, 1))
                .actualUsage(BigDecimal.valueOf(250))
                .baselineCost(BigDecimal.valueOf(50000))
                .actualCost(BigDecimal.valueOf(45000))
                .savingsAchieved(BigDecimal.valueOf(5000))
                .build();
        ReflectionTestUtils.setField(testTracking, "trackingId", 1L);
    }

    @Test
    @DisplayName("절감 추적 시작 성공")
    void startTracking_Success() {
        // given
        UsageData mockUsageData = UsageData.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(300))
                .currentCharge(BigDecimal.valueOf(50000))
                .measuredAt(LocalDateTime.now().minusMonths(1))
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(recommendationRepository.findById(anyLong())).willReturn(Optional.of(testRecommendation));
        given(usageDataRepository.findByUserAndMeasuredAtBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class))).willReturn(Arrays.asList(mockUsageData));
        given(savingsTrackingRepository.save(any(SavingsTracking.class))).willReturn(testTracking);

        // when
        SavingsTrackingResponse result = savingsTrackingService.startTracking(1L, 1L);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(recommendationRepository).findById(1L);
        verify(savingsTrackingRepository).save(any(SavingsTracking.class));
    }

    @Test
    @DisplayName("다른 사용자의 추천으로 추적 시작 실패")
    void startTracking_Forbidden() {
        // given
        User anotherUser = User.builder()
                .email("another@example.com")
                .passwordHash("password")
                .name("다른사용자")
                .phone("010-9999-9999")
                .address("부산")
                .dateOfBirth("1995-01-01")
                .userVerificationCode("CODE2")
                .build();
        ReflectionTestUtils.setField(anotherUser, "userId", 2L);

        Recommendation anotherRec = Recommendation.builder()
                .user(anotherUser)
                .utilityType(UtilityType.ELECTRICITY)
                .isApplied(false)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(recommendationRepository.findById(anyLong())).willReturn(Optional.of(anotherRec));

        // when & then
        assertThatThrownBy(() -> savingsTrackingService.startTracking(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(savingsTrackingRepository, never()).save(any(SavingsTracking.class));
    }

    @Test
    @DisplayName("사용자 추적 목록 조회 성공")
    void getUserTrackings_Success() {
        // given
        List<SavingsTracking> trackings = Arrays.asList(testTracking);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(savingsTrackingRepository.findByUser(any(User.class))).willReturn(trackings);

        // when
        List<SavingsTrackingResponse> result = savingsTrackingService.getUserTrackings(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(savingsTrackingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("기간별 추적 목록 조회 성공")
    void getTrackingsByPeriod_Success() {
        // given
        LocalDate startMonth = LocalDate.of(2025, 1, 1);
        LocalDate endMonth = LocalDate.of(2025, 12, 31);
        List<SavingsTracking> trackings = Arrays.asList(testTracking);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(savingsTrackingRepository.findByUserAndTrackingMonthBetween(any(User.class), any(LocalDate.class),
                any(LocalDate.class))).willReturn(trackings);

        // when
        List<SavingsTrackingResponse> result = savingsTrackingService.getTrackingsByPeriod(1L, startMonth, endMonth);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(savingsTrackingRepository).findByUserAndTrackingMonthBetween(testUser, startMonth, endMonth);
    }

    @Test
    @DisplayName("총 절감액 계산 성공")
    void getTotalSavings_Success() {
        // given
        SavingsTracking tracking1 = SavingsTracking.builder()
                .user(testUser)
                .savingsAchieved(BigDecimal.valueOf(5000))
                .build();
        SavingsTracking tracking2 = SavingsTracking.builder()
                .user(testUser)
                .savingsAchieved(BigDecimal.valueOf(3000))
                .build();
        List<SavingsTracking> trackings = Arrays.asList(tracking1, tracking2);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(savingsTrackingRepository.findByUser(any(User.class))).willReturn(trackings);

        // when
        BigDecimal result = savingsTrackingService.getTotalSavings(1L);

        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(8000));
        verify(savingsTrackingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("절감 내역 없을 때 총 절감액 0")
    void getTotalSavings_NoSavings() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(savingsTrackingRepository.findByUser(any(User.class))).willReturn(Collections.emptyList());

        // when
        BigDecimal result = savingsTrackingService.getTotalSavings(1L);

        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 추적 조회 실패")
    void getUserTrackings_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> savingsTrackingService.getUserTrackings(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(savingsTrackingRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("추적 업데이트 성공")
    void updateTracking_Success() {
        // given
        UsageData mockUsageData = UsageData.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .usageAmount(BigDecimal.valueOf(250))
                .currentCharge(BigDecimal.valueOf(45000))
                .measuredAt(LocalDateTime.now())
                .build();

        given(savingsTrackingRepository.findById(anyLong())).willReturn(Optional.of(testTracking));
        given(usageDataRepository.findByUserAndMeasuredAtBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class))).willReturn(Arrays.asList(mockUsageData));
        given(savingsTrackingRepository.save(any(SavingsTracking.class))).willReturn(testTracking);

        // when
        SavingsTrackingResponse result = savingsTrackingService.updateTracking(1L);

        // then
        assertThat(result).isNotNull();
        verify(savingsTrackingRepository).findById(1L);
        verify(savingsTrackingRepository).save(any(SavingsTracking.class));
    }

    @Test
    @DisplayName("존재하지 않는 추적 업데이트 실패")
    void updateTracking_NotFound() {
        // given
        given(savingsTrackingRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> savingsTrackingService.updateTracking(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAVINGS_TRACKING_NOT_FOUND);

        verify(savingsTrackingRepository, never()).save(any(SavingsTracking.class));
    }
}