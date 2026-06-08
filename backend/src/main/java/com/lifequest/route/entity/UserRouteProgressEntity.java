package com.lifequest.route.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.RouteProgressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "user_route_progress")
public class UserRouteProgressEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "current_chapter_id")
    private Long currentChapterId;

    @Column(name = "current_level_id")
    private Long currentLevelId;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "completed_level_count", nullable = false)
    private Integer completedLevelCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RouteProgressStatus status = RouteProgressStatus.IN_PROGRESS;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Long getCurrentChapterId() {
        return currentChapterId;
    }

    public void setCurrentChapterId(Long currentChapterId) {
        this.currentChapterId = currentChapterId;
    }

    public Long getCurrentLevelId() {
        return currentLevelId;
    }

    public void setCurrentLevelId(Long currentLevelId) {
        this.currentLevelId = currentLevelId;
    }

    public BigDecimal getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getCompletedLevelCount() {
        return completedLevelCount;
    }

    public void setCompletedLevelCount(Integer completedLevelCount) {
        this.completedLevelCount = completedLevelCount;
    }

    public RouteProgressStatus getStatus() {
        return status;
    }

    public void setStatus(RouteProgressStatus status) {
        this.status = status;
    }
}
