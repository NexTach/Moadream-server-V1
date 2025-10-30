package com.nextech.moadream.server.v1.domain.analysis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

import com.nextech.moadream.server.v1.domain.analysis.dto.RecommendationResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService 테스트")
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private UsagePatternRepository usagePatternRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private Recommendation testRecommendation;

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
                .isApplied(false)
                .build();
        ReflectionTestUtils.setField(testRecommendation, "recId", 1L);
    }

    @Test
    @DisplayName("사용자 추천 조회 성공")
    void getUserRecommendations_Success() {
        // given
        List<Recommendation> recommendations = Arrays.asList(testRecommendation);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(recommendationRepository.findByUser(any(User.class))).willReturn(recommendations);

        // when
        List<RecommendationResponse> result = recommendationService.getUserRecommendations(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(recommendationRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("미적용 추천 조회 성공")
    void getUnappliedRecommendations_Success() {
        // given
        List<Recommendation> recommendations = Arrays.asList(testRecommendation);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(recommendationRepository.findByUserAndIsApplied(any(User.class), eq(false))).willReturn(recommendations);

        // when
        List<RecommendationResponse> result = recommendationService.getUnappliedRecommendations(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(recommendationRepository).findByUserAndIsApplied(testUser, false);
    }

    @Test
    @DisplayName("추천 적용 처리 성공")
    void markAsApplied_Success() {
        // given
        given(recommendationRepository.findById(anyLong())).willReturn(Optional.of(testRecommendation));

        // when
        RecommendationResponse result = recommendationService.markAsApplied(1L);

        // then
        assertThat(result).isNotNull();
        verify(recommendationRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 추천 적용 처리 실패")
    void markAsApplied_NotFound() {
        // given
        given(recommendationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendationService.markAsApplied(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECOMMENDATION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 추천 조회 실패")
    void getUserRecommendations_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendationService.getUserRecommendations(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(recommendationRepository, never()).findByUser(any(User.class));
    }
}