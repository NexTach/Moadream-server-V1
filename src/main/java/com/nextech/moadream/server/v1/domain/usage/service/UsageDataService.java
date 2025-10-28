package com.nextech.moadream.server.v1.domain.usage.service;

import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageDataService {

    private final UsageDataRepository usageDataRepository;
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UsageAlertRepository usageAlertRepository;

    @Transactional
    public UsageDataResponse createUsageData(Long userId, UsageDataRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UsageData usageData = UsageData.builder()
                .user(user)
                .utilityType(request.getUtilityType())
                .usageAmount(request.getUsageAmount())
                .unit(request.getUnit())
                .currentCharge(request.getCurrentCharge())
                .measuredAt(request.getMeasuredAt())
                .build();

        UsageData savedUsageData = usageDataRepository.save(usageData);

        checkThresholdAndCreateAlert(user, request.getUtilityType(), request.getMeasuredAt());

        return UsageDataResponse.from(savedUsageData);
    }

    private void checkThresholdAndCreateAlert(User user, UtilityType utilityType, LocalDateTime measuredAt) {
        UserSetting userSetting = userSettingRepository.findByUser(user).orElse(null);
        if (userSetting == null || userSetting.getAlertThreshold() == null || userSetting.getMonthlyBudget() == null) {
            return;
        }

        YearMonth yearMonth = YearMonth.from(measuredAt);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<UsageData> monthlyUsageData = usageDataRepository
                .findByUserAndMeasuredAtBetween(user, startOfMonth, endOfMonth);
        BigDecimal totalCharge = monthlyUsageData.stream()
                .filter(data -> data.getCurrentCharge() != null)
                .map(UsageData::getCurrentCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal usagePercentage = totalCharge
                .divide(userSetting.getMonthlyBudget(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (usagePercentage.compareTo(userSetting.getAlertThreshold()) >= 0) {
            String alertMessage = String.format(
                    "%s 사용량이 월 예산의 %.1f%%에 도달했습니다. (%.0f원/%.0f원)",
                    getUtilityTypeKoreanName(utilityType),
                    usagePercentage,
                    totalCharge,
                    userSetting.getMonthlyBudget()
            );

            UsageAlert alert = UsageAlert.builder()
                    .user(user)
                    .utilityType(utilityType)
                    .alertType(AlertType.BUDGET_EXCEEDED)
                    .alertMessage(alertMessage)
                    .isRead(false)
                    .build();

            usageAlertRepository.save(alert);
        }
    }

    private String getUtilityTypeKoreanName(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "전기";
            case WATER -> "수도";
            case GAS -> "가스";
        };
    }

    public List<UsageDataResponse> getUserUsageData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageDataRepository.findByUser(user).stream()
                .map(UsageDataResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageDataResponse> getUserUsageDataByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageDataRepository.findByUserAndUtilityType(user, utilityType).stream()
                .map(UsageDataResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageDataResponse> getUserUsageDataByDateRange(
            Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return usageDataRepository.findByUserAndMeasuredAtBetween(user, startDate, endDate).stream()
                .map(UsageDataResponse::from)
                .collect(Collectors.toList());
    }

    public UsageDataResponse getLatestUsageData(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UsageData latestUsageData = usageDataRepository
                .findLatestByUserAndUtilityType(user, utilityType);

        if (latestUsageData == null) {
            throw new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND);
        }

        return UsageDataResponse.from(latestUsageData);
    }

    @Transactional
    public UsageDataResponse updateUsageData(Long userId, Long usageId, UsageDataRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UsageData usageData = usageDataRepository.findById(usageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND));

        if (!usageData.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND);
        }

        usageData.updateUsageData(
                request.getUtilityType(),
                request.getUsageAmount(),
                request.getUnit(),
                request.getCurrentCharge(),
                request.getMeasuredAt()
        );

        return UsageDataResponse.from(usageData);
    }
}