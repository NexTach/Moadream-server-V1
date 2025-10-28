package com.nextech.moadream.server.v1.domain.usage.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageAlertService {

    private final UsageAlertRepository usageAlertRepository;
    private final UserRepository userRepository;

    @Transactional
    public UsageAlertResponse createAlert(Long userId, UsageAlertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UsageAlert alert = UsageAlert.builder()
                .user(user)
                .utilityType(request.getUtilityType())
                .alertType(request.getAlertType())
                .alertMessage(request.getAlertMessage())
                .isRead(false)
                .build();

        UsageAlert savedAlert = usageAlertRepository.save(alert);
        return UsageAlertResponse.from(savedAlert);
    }

    public List<UsageAlertResponse> getUserAlerts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageAlertRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(UsageAlertResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageAlertResponse> getUnreadAlerts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageAlertRepository.findByUserAndIsRead(user, false).stream()
                .map(UsageAlertResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageAlertResponse> getAlertsByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageAlertRepository.findByUserAndUtilityType(user, utilityType).stream()
                .map(UsageAlertResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageAlertResponse> getAlertsByAlertType(Long userId, AlertType alertType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageAlertRepository.findByUserAndAlertType(user, alertType).stream()
                .map(UsageAlertResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsageAlertResponse markAlertAsRead(Long alertId) {
        UsageAlert alert = usageAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));

        alert.markAsRead();
        return UsageAlertResponse.from(alert);
    }

    @Transactional
    public void markAllAlertsAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UsageAlert> unreadAlerts = usageAlertRepository.findByUserAndIsRead(user, false);
        unreadAlerts.forEach(UsageAlert::markAsRead);
    }
}