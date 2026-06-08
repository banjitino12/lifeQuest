package com.lifequest.gamification.entity;

import com.lifequest.common.entity.CreatedOnlyEntity;
import com.lifequest.common.enums.GameEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "game_event")
public class GameEventEntity extends CreatedOnlyEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_log_id")
    private Long dailyLogId;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private GameEventType eventType;

    @Column(name = "event_code", length = 64)
    private String eventCode;

    @Column(name = "event_name", nullable = false, length = 128)
    private String eventName;

    @Column(name = "event_level")
    private Integer eventLevel;

    @Column(name = "event_description", length = 1024)
    private String eventDescription;

    @Lob
    @Column(name = "effect_json", columnDefinition = "json")
    private String effectJson;

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

    public GameEventType getEventType() {
        return eventType;
    }

    public void setEventType(GameEventType eventType) {
        this.eventType = eventType;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getEventLevel() {
        return eventLevel;
    }

    public void setEventLevel(Integer eventLevel) {
        this.eventLevel = eventLevel;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEffectJson() {
        return effectJson;
    }

    public void setEffectJson(String effectJson) {
        this.effectJson = effectJson;
    }
}
