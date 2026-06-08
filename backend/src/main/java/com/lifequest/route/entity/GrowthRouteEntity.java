package com.lifequest.route.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.GoalType;
import com.lifequest.common.enums.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "growth_route")
public class GrowthRouteEntity extends BaseEntity {

    @Column(name = "route_code", nullable = false, length = 64)
    private String routeCode;

    @Column(name = "route_name", nullable = false, length = 128)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 64)
    private GoalType goalType;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean defaultRoute;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RecordStatus status = RecordStatus.ACTIVE;

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultRoute() {
        return defaultRoute;
    }

    public void setDefaultRoute(boolean defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }
}
