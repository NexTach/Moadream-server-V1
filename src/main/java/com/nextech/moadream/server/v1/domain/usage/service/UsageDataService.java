package com.nextech.moadream.server.v1.domain.usage.service;

import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageDataService {

    private final UsageDataRepository usageDataRepository;
    private final UserRepository userRepository;

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
        return UsageDataResponse.from(savedUsageData);
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
}