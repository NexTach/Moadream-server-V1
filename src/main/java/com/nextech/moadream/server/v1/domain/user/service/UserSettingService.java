package com.nextech.moadream.server.v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.user.dto.UserSettingRequest;
import com.nextech.moadream.server.v1.domain.user.dto.UserSettingResponse;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    public UserSettingResponse getUserSetting(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        return UserSettingResponse.from(setting);
    }

    @Transactional
    public UserSettingResponse createUserSetting(Long userId, UserSettingRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userSettingRepository.findByUser(user).isPresent()) {
            throw new BusinessException(ErrorCode.SETTING_ALREADY_EXISTS);
        }

        UserSetting setting = UserSetting.builder().user(user).monthlyBudget(request.getMonthlyBudget())
                .alertThreshold(request.getAlertThreshold()).pushEnabled(request.getPushEnabled())
                .emailEnabled(request.getEmailEnabled()).build();

        UserSetting savedSetting = userSettingRepository.save(setting);
        return UserSettingResponse.from(savedSetting);
    }

    @Transactional
    public UserSettingResponse updateBudgetSettings(Long userId, UserSettingRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        setting.updateBudgetSettings(request.getMonthlyBudget(), request.getAlertThreshold());
        return UserSettingResponse.from(setting);
    }

    @Transactional
    public UserSettingResponse updateNotificationSettings(Long userId, UserSettingRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        setting.updateNotificationSettings(request.getPushEnabled(), request.getEmailEnabled());
        return UserSettingResponse.from(setting);
    }

    @Transactional
    public UserSettingResponse updateUserSetting(Long userId, UserSettingRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        if (request.getMonthlyBudget() != null || request.getAlertThreshold() != null) {
            setting.updateBudgetSettings(
                    request.getMonthlyBudget() != null ? request.getMonthlyBudget() : setting.getMonthlyBudget(),
                    request.getAlertThreshold() != null ? request.getAlertThreshold() : setting.getAlertThreshold());
        }

        if (request.getPushEnabled() != null || request.getEmailEnabled() != null) {
            setting.updateNotificationSettings(
                    request.getPushEnabled() != null ? request.getPushEnabled() : setting.getPushEnabled(),
                    request.getEmailEnabled() != null ? request.getEmailEnabled() : setting.getEmailEnabled());
        }

        return UserSettingResponse.from(setting);
    }
}
