package com.nextech.moadream.server.v1.domain.usage.entity;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageData {

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
    public UsageData(User user, UtilityType utilityType, BigDecimal usageAmount,
                     String unit, BigDecimal currentCharge, LocalDateTime measuredAt) {
        this.user = user;
        this.utilityType = utilityType;
        this.usageAmount = usageAmount;
        this.unit = unit;
        this.currentCharge = currentCharge;
        this.measuredAt = measuredAt;
    }
}