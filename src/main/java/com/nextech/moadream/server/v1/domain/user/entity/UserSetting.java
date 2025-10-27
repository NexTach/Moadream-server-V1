package com.nextech.moadream.server.v1.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "monthly_budget", precision = 10, scale = 2)
    private BigDecimal monthlyBudget;

    @Column(name = "alert_threshold", precision = 5, scale = 2)
    private BigDecimal alertThreshold;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "efficiency_score", precision = 5, scale = 2)
    private BigDecimal efficiencyScore;

    @Builder
    public UserSetting(User user, BigDecimal monthlyBudget, BigDecimal alertThreshold,
                       Boolean pushEnabled, Boolean emailEnabled, BigDecimal efficiencyScore) {
        this.user = user;
        this.monthlyBudget = monthlyBudget;
        this.alertThreshold = alertThreshold;
        this.pushEnabled = pushEnabled != null ? pushEnabled : true;
        this.emailEnabled = emailEnabled != null ? emailEnabled : true;
        this.efficiencyScore = efficiencyScore;
    }

    public void updateBudgetSettings(BigDecimal monthlyBudget, BigDecimal alertThreshold) {
        this.monthlyBudget = monthlyBudget;
        this.alertThreshold = alertThreshold;
    }

    public void updateNotificationSettings(Boolean pushEnabled, Boolean emailEnabled) {
        this.pushEnabled = pushEnabled;
        this.emailEnabled = emailEnabled;
    }

    public void updateEfficiencyScore(BigDecimal efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }
}