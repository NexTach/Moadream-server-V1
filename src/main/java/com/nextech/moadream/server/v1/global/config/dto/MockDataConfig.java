package com.nextech.moadream.server.v1.global.config.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class MockDataConfig {

    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String userVerificationCode;
    private String provider;
    private UserSettingDto settings;
    private List<UserBillDto> bills;
    private List<ElectricityBillDto> electricityBills;
    private List<GasBillDto> gasBills;
    private List<WaterBillDto> waterBills;
    private List<UsagePatternDto> usagePatterns;
    private List<RecommendationDto> recommendations;
    private List<AlertDto> alerts;
    private List<ChatSessionDto> chatSessions;

    @Data
    public static class UserSettingDto {
        private BigDecimal monthlyBudget;
        private BigDecimal alertThreshold;
        private Boolean pushEnabled;
        private Boolean emailEnabled;
        private BigDecimal efficiencyScore;
    }

    @Data
    public static class UserBillDto {
        private String utilityType;
        private String billNumber;
        private String generationName;
        private Boolean isVerified;
    }

    @Data
    public static class ElectricityBillDto {
        private Integer monthsAgo;
        private BigDecimal totalUsage;
        private BigDecimal energyCharge;
        private BigDecimal totalCharge;
        private BigDecimal basicCharge;
        private Boolean isPaid;
    }

    @Data
    public static class GasBillDto {
        private Integer monthsAgo;
        private BigDecimal totalUsage;
        private BigDecimal supplyPrice;
        private BigDecimal totalCharge;
        private Boolean isPaid;
    }

    @Data
    public static class WaterBillDto {
        private Integer monthsAgo;
        private BigDecimal totalUsage;
        private BigDecimal totalCharge;
        private Boolean isPaid;
    }

    @Data
    public static class UsagePatternDto {
        private String utilityType;
        private String frequencyType;
        private BigDecimal averageUsage;
        private BigDecimal peakUsage;
        private BigDecimal offPeakUsage;
        private String trend;
    }

    @Data
    public static class RecommendationDto {
        private String utilityType;
        private String recType;
        private String recommendationText;
        private BigDecimal expectedSavings;
        private String implementationDifficulty;
        private Boolean isApplied;
    }

    @Data
    public static class AlertDto {
        private String utilityType;
        private String alertType;
        private String alertMessage;
        private Boolean isRead;
    }

    @Data
    public static class ChatSessionDto {
        private String sessionTitle;
        private List<ChatMessageDto> messages;
    }

    @Data
    public static class ChatMessageDto {
        private String role;
        private String content;
    }
}
