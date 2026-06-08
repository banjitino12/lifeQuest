package com.lifequest.profile.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.FeedbackStyle;
import com.lifequest.common.enums.GoalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "user_profile")
public class UserProfileEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 64)
    private GoalType goalType;

    @Column(name = "current_goal", nullable = false)
    private String currentGoal;

    @Column(name = "goal_period", length = 64)
    private String goalPeriod;

    @Column(name = "weekly_plan_hours", precision = 5, scale = 2)
    private BigDecimal weeklyPlanHours;

    @Column(name = "current_stage", length = 512)
    private String currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_style", nullable = false, length = 64)
    private FeedbackStyle feedbackStyle;

    @Column(name = "route_id")
    private Long routeId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public String getCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(String currentGoal) {
        this.currentGoal = currentGoal;
    }

    public String getGoalPeriod() {
        return goalPeriod;
    }

    public void setGoalPeriod(String goalPeriod) {
        this.goalPeriod = goalPeriod;
    }

    public BigDecimal getWeeklyPlanHours() {
        return weeklyPlanHours;
    }

    public void setWeeklyPlanHours(BigDecimal weeklyPlanHours) {
        this.weeklyPlanHours = weeklyPlanHours;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public FeedbackStyle getFeedbackStyle() {
        return feedbackStyle;
    }

    public void setFeedbackStyle(FeedbackStyle feedbackStyle) {
        this.feedbackStyle = feedbackStyle;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
