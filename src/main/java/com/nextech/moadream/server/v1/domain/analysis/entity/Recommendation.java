package com.nextech.moadream.server.v1.domain.analysis.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rec_id")
    private Long recId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rec_type", nullable = false, length = 30)
    private RecommendationType recType;

    @Column(name = "recommendation_text", nullable = false, columnDefinition = "TEXT")
    private String recommendationText;

    @Column(name = "expected_savings", precision = 10, scale = 2)
    private BigDecimal expectedSavings;

    @Column(name = "implementation_difficulty", length = 20)
    private String implementationDifficulty;

    @Column(name = "is_applied", nullable = false)
    private Boolean isApplied = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingsTracking> savingsTrackings = new ArrayList<>();

    @Builder
    public Recommendation(User user, UtilityType utilityType, RecommendationType recType, String recommendationText,
            BigDecimal expectedSavings, String implementationDifficulty, Boolean isApplied) {
        this.user = user;
        this.utilityType = utilityType;
        this.recType = recType;
        this.recommendationText = recommendationText;
        this.expectedSavings = expectedSavings;
        this.implementationDifficulty = implementationDifficulty;
        this.isApplied = isApplied != null ? isApplied : false;
    }

    public void markAsApplied() {
        this.isApplied = true;
    }
}
