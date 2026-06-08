package com.lifequest.dailylog.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.DailyLogSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_log")
public class DailyLogEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Lob
    @Column(name = "raw_text")
    private String rawText;

    @Column(name = "study_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal studyHours = BigDecimal.ZERO;

    @Column(name = "work_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal workHours = BigDecimal.ZERO;

    @Column(name = "sleep_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal sleepHours = BigDecimal.ZERO;

    @Column(name = "exercise_minutes", nullable = false)
    private Integer exerciseMinutes = 0;

    @Column(name = "entertainment_minutes", nullable = false)
    private Integer entertainmentMinutes = 0;

    @Column(name = "mood_tag", length = 64)
    private String moodTag;

    @Column(name = "task_completion_rate")
    private Integer taskCompletionRate;

    @Lob
    @Column(name = "completed_content")
    private String completedContent;

    @Lob
    @Column(name = "problem_text")
    private String problemText;

    @Lob
    @Column(name = "reflection_text")
    private String reflectionText;

    @Lob
    @Column(name = "parsed_json", columnDefinition = "json")
    private String parsedJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private DailyLogSourceType sourceType = DailyLogSourceType.FORM;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public BigDecimal getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(BigDecimal studyHours) {
        this.studyHours = studyHours;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public BigDecimal getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(BigDecimal sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Integer getExerciseMinutes() {
        return exerciseMinutes;
    }

    public void setExerciseMinutes(Integer exerciseMinutes) {
        this.exerciseMinutes = exerciseMinutes;
    }

    public Integer getEntertainmentMinutes() {
        return entertainmentMinutes;
    }

    public void setEntertainmentMinutes(Integer entertainmentMinutes) {
        this.entertainmentMinutes = entertainmentMinutes;
    }

    public String getMoodTag() {
        return moodTag;
    }

    public void setMoodTag(String moodTag) {
        this.moodTag = moodTag;
    }

    public Integer getTaskCompletionRate() {
        return taskCompletionRate;
    }

    public void setTaskCompletionRate(Integer taskCompletionRate) {
        this.taskCompletionRate = taskCompletionRate;
    }

    public String getCompletedContent() {
        return completedContent;
    }

    public void setCompletedContent(String completedContent) {
        this.completedContent = completedContent;
    }

    public String getProblemText() {
        return problemText;
    }

    public void setProblemText(String problemText) {
        this.problemText = problemText;
    }

    public String getReflectionText() {
        return reflectionText;
    }

    public void setReflectionText(String reflectionText) {
        this.reflectionText = reflectionText;
    }

    public String getParsedJson() {
        return parsedJson;
    }

    public void setParsedJson(String parsedJson) {
        this.parsedJson = parsedJson;
    }

    public DailyLogSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DailyLogSourceType sourceType) {
        this.sourceType = sourceType;
    }
}
