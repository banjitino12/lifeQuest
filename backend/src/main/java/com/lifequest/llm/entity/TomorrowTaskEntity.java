package com.lifequest.llm.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.GeneratedBy;
import com.lifequest.common.enums.TomorrowTaskStatus;
import com.lifequest.common.enums.TomorrowTaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "tomorrow_task")
public class TomorrowTaskEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_daily_log_id")
    private Long sourceDailyLogId;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 64)
    private TomorrowTaskType taskType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1024)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TomorrowTaskStatus status = TomorrowTaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "generated_by", nullable = false, length = 32)
    private GeneratedBy generatedBy = GeneratedBy.RULE;
}
