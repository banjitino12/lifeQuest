package com.lifequest.weekly.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.WeeklyReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_report")
public class WeeklyReportEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "highest_score", precision = 5, scale = 2)
    private BigDecimal highestScore;

    @Column(name = "lowest_score", precision = 5, scale = 2)
    private BigDecimal lowestScore;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "main_enemy", length = 128)
    private String mainEnemy;

    @Lob
    @Column(name = "growth_summary")
    private String growthSummary;

    @Lob
    @Column(name = "suggestion_text")
    private String suggestionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WeeklyReportStatus status = WeeklyReportStatus.GENERATED;
}
