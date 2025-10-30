package com.nextech.moadream.server.v1.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextech.moadream.server.v1.domain.user.dto.UserSettingRequest;
import com.nextech.moadream.server.v1.domain.user.dto.UserSettingResponse;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSettingService 테스트")
class UserSettingServiceTest {

    @Mock
    private UserSettingRepository userSettingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSettingService userSettingService;

    private User testUser;
    private UserSetting testSetting;
    private UserSettingRequest settingRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("test@example.com").passwordHash("password").name("테스트").phone("010-1234-5678")
                .address("서울").dateOfBirth("1990-01-01").userVerificationCode("CODE").build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        testSetting = UserSetting.builder().user(testUser).monthlyBudget(BigDecimal.valueOf(100000))
                .alertThreshold(BigDecimal.valueOf(80)).pushEnabled(true).emailEnabled(true).build();
        ReflectionTestUtils.setField(testSetting, "settingId", 1L);

        settingRequest = new UserSettingRequest(BigDecimal.valueOf(100000), BigDecimal.valueOf(80), true, true);
    }

    @Test
    @DisplayName("사용자 설정 조회 성공")
    void getUserSetting_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.of(testSetting));

        // when
        UserSettingResponse result = userSettingService.getUserSetting(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMonthlyBudget()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        verify(userRepository).findById(1L);
        verify(userSettingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("사용자 설정이 없을 때 조회 실패")
    void getUserSetting_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userSettingService.getUserSetting(1L)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SETTING_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 설정 생성 성공")
    void createUserSetting_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.empty());
        given(userSettingRepository.save(any(UserSetting.class))).willReturn(testSetting);

        // when
        UserSettingResponse result = userSettingService.createUserSetting(1L, settingRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMonthlyBudget()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        verify(userRepository).findById(1L);
        verify(userSettingRepository).save(any(UserSetting.class));
    }

    @Test
    @DisplayName("이미 설정이 존재할 때 생성 실패")
    void createUserSetting_AlreadyExists() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.of(testSetting));

        // when & then
        assertThatThrownBy(() -> userSettingService.createUserSetting(1L, settingRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SETTING_ALREADY_EXISTS);

        verify(userSettingRepository, never()).save(any(UserSetting.class));
    }

    @Test
    @DisplayName("예산 설정 업데이트 성공")
    void updateBudgetSettings_Success() {
        // given
        UserSettingRequest updateRequest = new UserSettingRequest(BigDecimal.valueOf(150000), BigDecimal.valueOf(90),
                null, null);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.of(testSetting));

        // when
        UserSettingResponse result = userSettingService.updateBudgetSettings(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userSettingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공")
    void updateNotificationSettings_Success() {
        // given
        UserSettingRequest updateRequest = new UserSettingRequest(null, null, false, true);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.of(testSetting));

        // when
        UserSettingResponse result = userSettingService.updateNotificationSettings(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userSettingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("전체 설정 업데이트 성공")
    void updateUserSetting_Success() {
        // given
        UserSettingRequest updateRequest = new UserSettingRequest(BigDecimal.valueOf(120000), BigDecimal.valueOf(85),
                false, false);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(userSettingRepository.findByUser(any(User.class))).willReturn(Optional.of(testSetting));

        // when
        UserSettingResponse result = userSettingService.updateUserSetting(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userSettingRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 설정 조회 실패")
    void getUserSetting_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userSettingService.getUserSetting(999L)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userSettingRepository, never()).findByUser(any(User.class));
    }
}
