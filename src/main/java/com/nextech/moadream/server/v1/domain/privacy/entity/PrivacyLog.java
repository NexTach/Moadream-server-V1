package com.nextech.moadream.server.v1.domain.privacy.entity;

import com.nextech.moadream.server.v1.domain.privacy.enums.ActionType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "privacy_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivacyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    @Column(name = "access_type", length = 50)
    private String accessType;

    @Column(name = "action_description", columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;

    @Column(name = "deletion_scheduled_at")
    private LocalDateTime deletionScheduledAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PrivacyLog(User user, ActionType actionType, String accessType,
                      String actionDescription, Integer retentionPeriodDays,
                      LocalDateTime deletionScheduledAt, Boolean isDeleted) {
        this.user = user;
        this.actionType = actionType;
        this.accessType = accessType;
        this.actionDescription = actionDescription;
        this.retentionPeriodDays = retentionPeriodDays;
        this.deletionScheduledAt = deletionScheduledAt;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}