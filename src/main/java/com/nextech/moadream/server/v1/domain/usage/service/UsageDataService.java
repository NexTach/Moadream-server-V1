package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyAverageUsageResponse;
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
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UsageData usageData = UsageData.builder().user(user).utilityType(request.getUtilityType())
                .usageAmount(request.getUsageAmount()).unit(request.getUnit()).currentCharge(request.getCurrentCharge())
                .measuredAt(request.getMeasuredAt()).build();
        UsageData savedUsageData = usageDataRepository.save(usageData);
        checkThresholdAndCreateAlert(user, request.getUtilityType(), request.getMeasuredAt());
        return UsageDataResponse.from(savedUsageData);
    }

    private void checkThresholdAndCreateAlert(User user, UtilityType utilityType, LocalDateTime measuredAt) {
        UserSetting userSetting = userSettingRepository.findByUser(user).orElse(null);
        if (userSetting == null) {
            return;
        }
        YearMonth yearMonth = YearMonth.from(measuredAt);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<UsageData> monthlyUsageData = usageDataRepository.findByUserAndMeasuredAtBetween(user, startOfMonth,
                endOfMonth);

        checkBudgetExceeded(user, utilityType, userSetting, monthlyUsageData);
        checkHighUsage(user, utilityType, measuredAt, monthlyUsageData);
        checkUnusualPattern(user, utilityType, measuredAt);
    }

    private void checkBudgetExceeded(User user, UtilityType utilityType, UserSetting userSetting,
            List<UsageData> monthlyUsageData) {
        if (userSetting.getAlertThreshold() == null || userSetting.getMonthlyBudget() == null) {
            return;
        }

        BigDecimal totalCharge = monthlyUsageData.stream().filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getCurrentCharge).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (userSetting.getMonthlyBudget().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal usagePercentage = totalCharge
                .divide(userSetting.getMonthlyBudget(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (usagePercentage.compareTo(userSetting.getAlertThreshold()) >= 0) {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            boolean recentAlertExists = usageAlertRepository.findByUserAndUtilityType(user, utilityType).stream()
                    .anyMatch(alert -> alert.getAlertType() == AlertType.BUDGET_EXCEEDED
                            && alert.getCreatedAt().isAfter(oneDayAgo));

            if (!recentAlertExists) {
                String alertMessage = String.format("%s 사용량이 월 예산의 %.1f%%에 도달했습니다. (%.0f원/%.0f원)",
                        getUtilityTypeKoreanName(utilityType), usagePercentage, totalCharge,
                        userSetting.getMonthlyBudget());
                UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType)
                        .alertType(AlertType.BUDGET_EXCEEDED).alertMessage(alertMessage).isRead(false).build();
                usageAlertRepository.save(alert);
            }
        }
    }

    private void checkHighUsage(User user, UtilityType utilityType, LocalDateTime measuredAt,
            List<UsageData> currentMonthData) {
        YearMonth currentMonth = YearMonth.from(measuredAt);
        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> lastMonthData = usageDataRepository.findByUserAndMeasuredAtBetween(user, lastMonthStart,
                lastMonthEnd);

        BigDecimal currentUsage = currentMonthData.stream().filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastMonthUsage = lastMonthData.stream().filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lastMonthUsage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal increasePercentage = currentUsage.subtract(lastMonthUsage)
                    .divide(lastMonthUsage, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            if (increasePercentage.compareTo(BigDecimal.valueOf(30)) >= 0) {
                LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
                boolean recentAlertExists = usageAlertRepository.findByUserAndUtilityType(user, utilityType).stream()
                        .anyMatch(alert -> alert.getAlertType() == AlertType.HIGH_USAGE
                                && alert.getCreatedAt().isAfter(oneDayAgo));

                if (!recentAlertExists) {
                    String alertMessage = String.format("%s 사용량이 전월 대비 %.1f%% 증가했습니다. 확인이 필요합니다.",
                            getUtilityTypeKoreanName(utilityType), increasePercentage);
                    UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType)
                            .alertType(AlertType.HIGH_USAGE).alertMessage(alertMessage).isRead(false).build();
                    usageAlertRepository.save(alert);
                }
            }
        }
    }

    private void checkUnusualPattern(User user, UtilityType utilityType, LocalDateTime measuredAt) {
        LocalDateTime sevenDaysAgo = measuredAt.minusDays(7);
        LocalDateTime oneDayAgo = measuredAt.minusDays(1);

        List<UsageData> recentSevenDays = usageDataRepository.findByUserAndMeasuredAtBetween(user, sevenDaysAgo,
                oneDayAgo);

        List<UsageData> todayData = usageDataRepository.findByUserAndMeasuredAtBetween(user, measuredAt.minusHours(24),
                measuredAt);

        BigDecimal recentAverage = recentSevenDays.stream().filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(7), 4, java.math.RoundingMode.HALF_UP);

        BigDecimal todayUsage = todayData.stream().filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (recentAverage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal increasePercentage = todayUsage.subtract(recentAverage)
                    .divide(recentAverage, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            if (increasePercentage.compareTo(BigDecimal.valueOf(50)) >= 0) {
                LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
                boolean recentAlertExists = usageAlertRepository.findByUserAndUtilityType(user, utilityType).stream()
                        .anyMatch(alert -> alert.getAlertType() == AlertType.UNUSUAL_PATTERN
                                && alert.getCreatedAt().isAfter(twoDaysAgo));

                if (!recentAlertExists) {
                    String alertMessage = String.format("%s에서 평소와 다른 사용 패턴이 감지되었습니다. 최근 7일 평균 대비 %.1f%% 증가했습니다.",
                            getUtilityTypeKoreanName(utilityType), increasePercentage);
                    UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType)
                            .alertType(AlertType.UNUSUAL_PATTERN).alertMessage(alertMessage).isRead(false).build();
                    usageAlertRepository.save(alert);
                }
            }
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
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return usageDataRepository.findByUser(user).stream().map(UsageDataResponse::from).collect(Collectors.toList());
    }

    public List<UsageDataResponse> getUserUsageDataByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return usageDataRepository.findByUserAndUtilityType(user, utilityType).stream().map(UsageDataResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsageDataResponse> getUserUsageDataByDateRange(Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return usageDataRepository.findByUserAndMeasuredAtBetween(user, startDate, endDate).stream()
                .map(UsageDataResponse::from).collect(Collectors.toList());
    }

    public UsageDataResponse getLatestUsageData(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UsageData latestUsageData = usageDataRepository.findLatestByUserAndUtilityType(user, utilityType);
        if (latestUsageData == null) {
            throw new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND);
        }
        return UsageDataResponse.from(latestUsageData);
    }

    @Transactional
    public UsageDataResponse updateUsageData(Long userId, Long usageId, UsageDataRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UsageData usageData = usageDataRepository.findById(usageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND));
        if (!usageData.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.USAGE_DATA_NOT_FOUND);
        }
        usageData.updateUsageData(request.getUtilityType(), request.getUsageAmount(), request.getUnit(),
                request.getCurrentCharge(), request.getMeasuredAt());
        return UsageDataResponse.from(usageData);
    }

    public List<MonthlyAverageUsageResponse> getMonthlyAverageUsageData(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UsageData> allUsageData = usageDataRepository.findByUser(user);

        return calculateMonthlyAverages(allUsageData);
    }

    public List<MonthlyAverageUsageResponse> getMonthlyAverageUsageDataByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UsageData> allUsageData = usageDataRepository.findByUserAndUtilityType(user, utilityType);

        return calculateMonthlyAverages(allUsageData);
    }

    public List<MonthlyAverageUsageResponse> getMonthlyAverageUsageDataByDateRange(Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UsageData> allUsageData = usageDataRepository.findByUserAndMeasuredAtBetween(user, startDate, endDate);

        return calculateMonthlyAverages(allUsageData);
    }

    private List<MonthlyAverageUsageResponse> calculateMonthlyAverages(List<UsageData> usageDataList) {
        Map<String, List<UsageData>> groupedData = usageDataList.stream().collect(Collectors.groupingBy(data -> {
            YearMonth yearMonth = YearMonth.from(data.getMeasuredAt());
            return yearMonth.getYear() + "-" + yearMonth.getMonthValue() + "-" + data.getUtilityType().name();
        }));

        List<MonthlyAverageUsageResponse> result = new ArrayList<>();

        groupedData.forEach((key, dataList) -> {
            if (dataList.isEmpty()) {
                return;
            }

            String[] parts = key.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            UtilityType utilityType = UtilityType.valueOf(parts[2]);

            BigDecimal totalUsage = dataList.stream().map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);

            BigDecimal totalCharge = dataList.stream().map(UsageData::getCurrentCharge).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = dataList.size();

            BigDecimal averageUsage = count > 0
                    ? totalUsage.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal averageCharge = count > 0
                    ? totalCharge.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String unit = dataList.get(0).getUnit();

            MonthlyAverageUsageResponse response = MonthlyAverageUsageResponse.builder().year(year).month(month)
                    .utilityType(utilityType).averageUsage(averageUsage).totalUsage(totalUsage)
                    .averageCharge(averageCharge).totalCharge(totalCharge).dataCount(count).unit(unit).build();

            result.add(response);
        });

        result.sort(Comparator.comparing(MonthlyAverageUsageResponse::getYear)
                .thenComparing(MonthlyAverageUsageResponse::getMonth)
                .thenComparing(response -> response.getUtilityType().name()));

        return result;
    }
}
