package com.nextech.moadream.server.v1.domain.analysis.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_patterns", indexes = {@Index(name = "idx_pattern_user_id", columnList = "user_id"),
        @Index(name = "idx_pattern_utility_type", columnList = "utility_type"),
        @Index(name = "idx_pattern_frequency", columnList = "frequency_type"),
        @Index(name = "idx_pattern_user_type", columnList = "user_id, utility_type")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UsagePattern implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long patternId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 20)
    private FrequencyType frequencyType;

    @Column(name = "average_usage", precision = 10, scale = 2)
    private BigDecimal averageUsage;

    @Column(name = "peak_usage", precision = 10, scale = 2)
    private BigDecimal peakUsage;

    @Column(name = "off_peak_usage", precision = 10, scale = 2)
    private BigDecimal offPeakUsage;

    @Column(name = "trend", length = 50)
    private String trend;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UsagePattern(User user, UtilityType utilityType, FrequencyType frequencyType, BigDecimal averageUsage,
            BigDecimal peakUsage, BigDecimal offPeakUsage, String trend) {
        this.user = user;
        this.utilityType = utilityType;
        this.frequencyType = frequencyType;
        this.averageUsage = averageUsage;
        this.peakUsage = peakUsage;
        this.offPeakUsage = offPeakUsage;
        this.trend = trend;
    }

    public void updatePattern(BigDecimal averageUsage, BigDecimal peakUsage, BigDecimal offPeakUsage, String trend) {
        this.averageUsage = averageUsage;
        this.peakUsage = peakUsage;
        this.offPeakUsage = offPeakUsage;
        this.trend = trend;
    }
}
