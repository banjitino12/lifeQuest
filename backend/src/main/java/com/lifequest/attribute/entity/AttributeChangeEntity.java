package com.lifequest.attribute.entity;

import com.lifequest.common.entity.CreatedOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "attribute_change")
public class AttributeChangeEntity extends CreatedOnlyEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_log_id", nullable = false)
    private Long dailyLogId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "focus_delta", nullable = false)
    private Integer focusDelta = 0;

    @Column(name = "discipline_delta", nullable = false)
    private Integer disciplineDelta = 0;

    @Column(name = "knowledge_delta", nullable = false)
    private Integer knowledgeDelta = 0;

    @Column(name = "energy_delta", nullable = false)
    private Integer energyDelta = 0;

    @Column(name = "mood_delta", nullable = false)
    private Integer moodDelta = 0;

    @Column(name = "execution_delta", nullable = false)
    private Integer executionDelta = 0;

    @Column(name = "balance_delta", nullable = false)
    private Integer balanceDelta = 0;

    @Column(name = "exp_delta", nullable = false)
    private Integer expDelta = 0;

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

    public Integer getFocusDelta() {
        return focusDelta;
    }

    public void setFocusDelta(Integer focusDelta) {
        this.focusDelta = focusDelta;
    }

    public Integer getDisciplineDelta() {
        return disciplineDelta;
    }

    public void setDisciplineDelta(Integer disciplineDelta) {
        this.disciplineDelta = disciplineDelta;
    }

    public Integer getKnowledgeDelta() {
        return knowledgeDelta;
    }

    public void setKnowledgeDelta(Integer knowledgeDelta) {
        this.knowledgeDelta = knowledgeDelta;
    }

    public Integer getEnergyDelta() {
        return energyDelta;
    }

    public void setEnergyDelta(Integer energyDelta) {
        this.energyDelta = energyDelta;
    }

    public Integer getMoodDelta() {
        return moodDelta;
    }

    public void setMoodDelta(Integer moodDelta) {
        this.moodDelta = moodDelta;
    }

    public Integer getExecutionDelta() {
        return executionDelta;
    }

    public void setExecutionDelta(Integer executionDelta) {
        this.executionDelta = executionDelta;
    }

    public Integer getBalanceDelta() {
        return balanceDelta;
    }

    public void setBalanceDelta(Integer balanceDelta) {
        this.balanceDelta = balanceDelta;
    }

    public Integer getExpDelta() {
        return expDelta;
    }

    public void setExpDelta(Integer expDelta) {
        this.expDelta = expDelta;
    }

    public String getReasonJson() {
        return reasonJson;
    }

    public void setReasonJson(String reasonJson) {
        this.reasonJson = reasonJson;
    }
}
