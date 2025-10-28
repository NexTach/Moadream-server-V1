package com.nextech.moadream.server.v1.domain.usage.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_data", indexes = {@Index(name = "idx_usage_user_id", columnList = "user_id"),
        @Index(name = "idx_usage_type", columnList = "utility_type"),
        @Index(name = "idx_usage_measured_at", columnList = "measured_at"),
        @Index(name = "idx_usage_user_type", columnList = "user_id, utility_type")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UsageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Column(name = "usage_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal usageAmount;

    @Column(name = "unit", nullable = false, length = 10)
    private String unit;

    @Column(name = "current_charge", precision = 10, scale = 2)
    private BigDecimal currentCharge;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UsageData(User user, UtilityType utilityType, BigDecimal usageAmount, String unit, BigDecimal currentCharge,
            LocalDateTime measuredAt) {
        this.user = user;
        this.utilityType = utilityType;
        this.usageAmount = usageAmount;
        this.unit = unit;
        this.currentCharge = currentCharge;
        this.measuredAt = measuredAt;
    }

    public void updateUsageData(UtilityType utilityType, BigDecimal usageAmount, String unit, BigDecimal currentCharge,
            LocalDateTime measuredAt) {
        this.utilityType = utilityType;
        this.usageAmount = usageAmount;
        this.unit = unit;
        this.currentCharge = currentCharge;
        this.measuredAt = measuredAt;
    }
}
