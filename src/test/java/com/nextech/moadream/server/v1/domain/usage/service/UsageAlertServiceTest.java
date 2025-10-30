package com.nextech.moadream.server.v1.domain.usage.service;

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

import com.nextech.moadream.server.v1.domain.usage.dto.UsageAlertRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageAlertResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsageAlertService 테스트")
class UsageAlertServiceTest {

    @Mock
    private UsageAlertRepository usageAlertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsageAlertService usageAlertService;

    private User testUser;
    private UsageAlert testAlert;
    private UsageAlertRequest alertRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("test@example.com").passwordHash("password").name("테스트").phone("010-1234-5678")
                .address("서울").dateOfBirth("1990-01-01").userVerificationCode("CODE").build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        testAlert = UsageAlert.builder().user(testUser).utilityType(UtilityType.ELECTRICITY)
                .alertType(AlertType.BUDGET_EXCEEDED).alertMessage("전기 사용량이 예산을 초과했습니다.").isRead(false).build();
        ReflectionTestUtils.setField(testAlert, "alertId", 1L);

        alertRequest = new UsageAlertRequest(UtilityType.ELECTRICITY, AlertType.BUDGET_EXCEEDED, "전기 사용량이 예산을 초과했습니다.");
    }

    @Test
    @DisplayName("알림 생성 성공")
    void createAlert_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.save(any(UsageAlert.class))).willReturn(testAlert);

        // when
        UsageAlertResponse result = usageAlertService.createAlert(1L, alertRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        assertThat(result.getAlertType()).isEqualTo(AlertType.BUDGET_EXCEEDED);
        assertThat(result.getAlertMessage()).isEqualTo("전기 사용량이 예산을 초과했습니다.");
        verify(userRepository).findById(1L);
        verify(usageAlertRepository).save(any(UsageAlert.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 알림 생성 실패")
    void createAlert_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usageAlertService.createAlert(999L, alertRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(usageAlertRepository, never()).save(any(UsageAlert.class));
    }

    @Test
    @DisplayName("사용자의 모든 알림 조회 성공")
    void getUserAlerts_Success() {
        // given
        List<UsageAlert> alerts = Arrays.asList(testAlert);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.findByUserOrderByCreatedAtDesc(any(User.class))).willReturn(alerts);

        // when
        List<UsageAlertResponse> result = usageAlertService.getUserAlerts(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(userRepository).findById(1L);
        verify(usageAlertRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("읽지 않은 알림 조회 성공")
    void getUnreadAlerts_Success() {
        // given
        List<UsageAlert> unreadAlerts = Arrays.asList(testAlert);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.findByUserAndIsRead(any(User.class), eq(false))).willReturn(unreadAlerts);

        // when
        List<UsageAlertResponse> result = usageAlertService.getUnreadAlerts(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsRead()).isFalse();
        verify(usageAlertRepository).findByUserAndIsRead(testUser, false);
    }

    @Test
    @DisplayName("유틸리티 타입별 알림 조회 성공")
    void getAlertsByType_Success() {
        // given
        List<UsageAlert> alerts = Arrays.asList(testAlert);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(alerts);

        // when
        List<UsageAlertResponse> result = usageAlertService.getAlertsByType(1L, UtilityType.ELECTRICITY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(usageAlertRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
    }

    @Test
    @DisplayName("알림 타입별 알림 조회 성공")
    void getAlertsByAlertType_Success() {
        // given
        List<UsageAlert> alerts = Arrays.asList(testAlert);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.findByUserAndAlertType(any(User.class), any(AlertType.class))).willReturn(alerts);

        // when
        List<UsageAlertResponse> result = usageAlertService.getAlertsByAlertType(1L, AlertType.BUDGET_EXCEEDED);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertType()).isEqualTo(AlertType.BUDGET_EXCEEDED);
        verify(usageAlertRepository).findByUserAndAlertType(testUser, AlertType.BUDGET_EXCEEDED);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAlertAsRead_Success() {
        // given
        given(usageAlertRepository.findById(anyLong())).willReturn(Optional.of(testAlert));

        // when
        UsageAlertResponse result = usageAlertService.markAlertAsRead(1L);

        // then
        assertThat(result).isNotNull();
        verify(usageAlertRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 실패")
    void markAlertAsRead_NotFound() {
        // given
        given(usageAlertRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usageAlertService.markAlertAsRead(999L)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALERT_NOT_FOUND);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void markAllAlertsAsRead_Success() {
        // given
        UsageAlert unreadAlert1 = UsageAlert.builder().user(testUser).utilityType(UtilityType.WATER)
                .alertType(AlertType.HIGH_USAGE).alertMessage("수도 사용량 급증").isRead(false).build();
        UsageAlert unreadAlert2 = UsageAlert.builder().user(testUser).utilityType(UtilityType.GAS)
                .alertType(AlertType.UNUSUAL_PATTERN).alertMessage("가스 사용량 이상 패턴").isRead(false).build();
        List<UsageAlert> unreadAlerts = Arrays.asList(unreadAlert1, unreadAlert2);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(usageAlertRepository.findByUserAndIsRead(any(User.class), eq(false))).willReturn(unreadAlerts);

        // when
        usageAlertService.markAllAlertsAsRead(1L);

        // then
        verify(userRepository).findById(1L);
        verify(usageAlertRepository).findByUserAndIsRead(testUser, false);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 모든 알림 읽음 처리 실패")
    void markAllAlertsAsRead_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> usageAlertService.markAllAlertsAsRead(999L)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(usageAlertRepository, never()).findByUserAndIsRead(any(User.class), anyBoolean());
    }
}
