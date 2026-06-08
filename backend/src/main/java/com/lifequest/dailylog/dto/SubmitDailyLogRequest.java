package com.lifequest.dailylog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record SubmitDailyLogRequest(
        @NotNull LocalDate logDate,
        String rawText,
        @PositiveOrZero Double studyHours,
        @PositiveOrZero Double workHours,
        @PositiveOrZero Double sleepHours,
        @PositiveOrZero Integer exerciseMinutes,
        @PositiveOrZero Integer entertainmentMinutes,
        String moodTag,
        @Min(0) @Max(100) Integer taskCompletionRate,
        String completedContent,
        String problemText,
        String reflectionText,
        String sourceType
) {
}
