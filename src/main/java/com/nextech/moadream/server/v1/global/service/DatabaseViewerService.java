package com.nextech.moadream.server.v1.global.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.SavingsTrackingRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatMessageRepository;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatSessionRepository;
import com.nextech.moadream.server.v1.domain.privacy.repository.PrivacyLogRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.ElectricityBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.GasBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.WaterBillRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserBillRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseViewerService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserBillRepository userBillRepository;
    private final UsageDataRepository usageDataRepository;
    private final ElectricityBillRepository electricityBillRepository;
    private final GasBillRepository gasBillRepository;
    private final WaterBillRepository waterBillRepository;
    private final MonthlyBillRepository monthlyBillRepository;
    private final UsageAlertRepository usageAlertRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsagePatternRepository usagePatternRepository;
    private final RecommendationRepository recommendationRepository;
    private final SavingsTrackingRepository savingsTrackingRepository;
    private final PrivacyLogRepository privacyLogRepository;

    public Map<String, Object> getAllDatabaseData() {
        Map<String, Object> data = new HashMap<>();

        data.put("users", userRepository.findAll());
        data.put("userSettings", userSettingRepository.findAll());
        data.put("userBills", userBillRepository.findAll());
        data.put("usageData", usageDataRepository.findAll());
        data.put("electricityBills", electricityBillRepository.findAll());
        data.put("gasBills", gasBillRepository.findAll());
        data.put("waterBills", waterBillRepository.findAll());
        data.put("monthlyBills", monthlyBillRepository.findAll());
        data.put("usageAlerts", usageAlertRepository.findAll());
        data.put("chatSessions", chatSessionRepository.findAll());
        data.put("chatMessages", chatMessageRepository.findAll());
        data.put("usagePatterns", usagePatternRepository.findAll());
        data.put("recommendations", recommendationRepository.findAll());
        data.put("savingsTrackings", savingsTrackingRepository.findAll());
        data.put("privacyLogs", privacyLogRepository.findAll());

        return data;
    }

    public Map<String, Long> getDatabaseStatistics() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("users", userRepository.count());
        stats.put("userSettings", userSettingRepository.count());
        stats.put("userBills", userBillRepository.count());
        stats.put("usageData", usageDataRepository.count());
        stats.put("electricityBills", electricityBillRepository.count());
        stats.put("gasBills", gasBillRepository.count());
        stats.put("waterBills", waterBillRepository.count());
        stats.put("monthlyBills", monthlyBillRepository.count());
        stats.put("usageAlerts", usageAlertRepository.count());
        stats.put("chatSessions", chatSessionRepository.count());
        stats.put("chatMessages", chatMessageRepository.count());
        stats.put("usagePatterns", usagePatternRepository.count());
        stats.put("recommendations", recommendationRepository.count());
        stats.put("savingsTrackings", savingsTrackingRepository.count());
        stats.put("privacyLogs", privacyLogRepository.count());

        return stats;
    }

    public Map<String, Object> getTableData(String tableName) {
        Map<String, Object> result = new HashMap<>();

        List<?> data = switch (tableName.toLowerCase()) {
            case "users" -> userRepository.findAll();
            case "usersettings" -> userSettingRepository.findAll();
            case "userbills" -> userBillRepository.findAll();
            case "usagedata" -> usageDataRepository.findAll();
            case "electricitybills" -> electricityBillRepository.findAll();
            case "gasbills" -> gasBillRepository.findAll();
            case "waterbills" -> waterBillRepository.findAll();
            case "monthlybills" -> monthlyBillRepository.findAll();
            case "usagealerts" -> usageAlertRepository.findAll();
            case "chatsessions" -> chatSessionRepository.findAll();
            case "chatmessages" -> chatMessageRepository.findAll();
            case "usagepatterns" -> usagePatternRepository.findAll();
            case "recommendations" -> recommendationRepository.findAll();
            case "savingstrackings" -> savingsTrackingRepository.findAll();
            case "privacylogs" -> privacyLogRepository.findAll();
            default -> List.of();
        };

        result.put("tableName", tableName);
        result.put("data", data);
        result.put("count", data.size());

        return result;
    }
}