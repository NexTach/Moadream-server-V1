package com.nextech.moadream.server.v1.domain.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "savings_tracking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingsTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rec_id")
    private Recommendation recommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Column(name = "tracking_month", nullable = false)
    private LocalDate trackingMonth;

    @Column(name = "actual_usage", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualUsage;

    @Column(name = "baseline_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal baselineCost;

    @Column(name = "actual_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "savings_achieved", precision = 10, scale = 2)
    private BigDecimal savingsAchieved;

    @Builder
    public SavingsTracking(User user, Recommendation recommendation, UtilityType utilityType, LocalDate trackingMonth,
            BigDecimal actualUsage, BigDecimal baselineCost, BigDecimal actualCost, BigDecimal savingsAchieved) {
        this.user = user;
        this.recommendation = recommendation;
        this.utilityType = utilityType;
        this.trackingMonth = trackingMonth;
        this.actualUsage = actualUsage;
        this.baselineCost = baselineCost;
        this.actualCost = actualCost;
        this.savingsAchieved = savingsAchieved;
    }

    public void calculateSavings() {
        if (this.baselineCost != null && this.actualCost != null) {
            this.savingsAchieved = this.baselineCost.subtract(this.actualCost);
        }
    }
}
