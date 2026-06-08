package com.lifequest.scoring.entity;

import com.lifequest.common.entity.CreatedOnlyEntity;
import com.lifequest.common.enums.DailyRating;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_score")
public class DailyScoreEntity extends CreatedOnlyEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_log_id", nullable = false)
    private Long dailyLogId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "daily_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal dailyScore;

    @Column(name = "growth_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal growthScore;

    @Column(name = "execution_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal executionScore;

    @Column(name = "energy_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal energyScore;

    @Column(name = "mood_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal moodScore;

    @Column(name = "distraction_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal distractionScore;

    @Column(name = "reflection_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal reflectionScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 8)
    private DailyRating rating;

    @Lob
    @Column(name = "reason_json", nullable = false, columnDefinition = "json")
    private String reasonJson;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDailyLogId() {
        return dailyLogId;
    }

    public void setDailyLogId(Long dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public BigDecimal getDailyScore() {
        return dailyScore;
    }

    public void setDailyScore(BigDecimal dailyScore) {
        this.dailyScore = dailyScore;
    }

    public BigDecimal getGrowthScore() {
        return growthScore;
    }

    public void setGrowthScore(BigDecimal growthScore) {
        this.growthScore = growthScore;
    }

    public BigDecimal getExecutionScore() {
        return executionScore;
    }

    public void setExecutionScore(BigDecimal executionScore) {
        this.executionScore = executionScore;
    }

    public BigDecimal getEnergyScore() {
        return energyScore;
    }

    public void setEnergyScore(BigDecimal energyScore) {
        this.energyScore = energyScore;
    }

    public BigDecimal getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(BigDecimal moodScore) {
        this.moodScore = moodScore;
    }

    public BigDecimal getDistractionScore() {
        return distractionScore;
    }

    public void setDistractionScore(BigDecimal distractionScore) {
        this.distractionScore = distractionScore;
    }

    public BigDecimal getReflectionScore() {
        return reflectionScore;
    }

    public void setReflectionScore(BigDecimal reflectionScore) {
        this.reflectionScore = reflectionScore;
    }

    public DailyRating getRating() {
        return rating;
    }

    public void setRating(DailyRating rating) {
        this.rating = rating;
    }

    public String getReasonJson() {
        return reasonJson;
    }

    public void setReasonJson(String reasonJson) {
        this.reasonJson = reasonJson;
    }
}
