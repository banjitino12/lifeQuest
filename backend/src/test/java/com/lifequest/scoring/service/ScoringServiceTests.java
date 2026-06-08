package com.lifequest.scoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.common.enums.DailyRating;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.scoring.entity.DailyScoreEntity;
import com.lifequest.scoring.model.ScoreResult;
import com.lifequest.scoring.repository.DailyScoreRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScoringServiceTests {

    private final DailyScoreRepository dailyScoreRepository = org.mockito.Mockito.mock(DailyScoreRepository.class);
    private final ScoringService scoringService = new ScoringService(dailyScoreRepository, new ObjectMapper());

    @Test
    void calculateReturnsWeightedScoreRatingAndReasonsForNormalInput() {
        DailyLogEntity dailyLog = dailyLog(7L, 101L, LocalDate.of(2026, 6, 9));
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setWorkHours(BigDecimal.valueOf(2));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        dailyLog.setMoodTag("开心");
        dailyLog.setTaskCompletionRate(90);
        dailyLog.setCompletedContent("完成 Redis 复习和项目接口");
        dailyLog.setReflectionText("今天学习状态稳定，上午推进 Redis 复习，下午完成接口实现，晚上能继续保持低娱乐和及时复盘。"
                + "后续计划把这套节奏固定下来，并在睡前提前收尾，减少临时切换任务带来的消耗。");

        ScoreResult result = scoringService.calculate(dailyLog);

        assertThat(result.dailyScore()).isEqualByComparingTo("96.00");
        assertThat(result.rating()).isEqualTo("S");
        assertThat(result.growthScore()).isEqualByComparingTo("100.00");
        assertThat(result.executionScore()).isEqualByComparingTo("90.00");
        assertThat(result.energyScore()).isEqualByComparingTo("100.00");
        assertThat(result.moodScore()).isEqualByComparingTo("90.00");
        assertThat(result.distractionScore()).isEqualByComparingTo("100.00");
        assertThat(result.reflectionScore()).isEqualByComparingTo("100.00");
        assertThat(result.reasons()).containsKeys(
                "growthScore",
                "executionScore",
                "energyScore",
                "moodScore",
                "distractionScore",
                "reflectionScore",
                "dailyScore",
                "rating"
        );
    }

    @Test
    void calculateHandlesBoundaryValuesAndLowRating() {
        DailyLogEntity dailyLog = dailyLog(7L, 102L, LocalDate.of(2026, 6, 10));
        dailyLog.setStudyHours(BigDecimal.ZERO);
        dailyLog.setWorkHours(BigDecimal.ZERO);
        dailyLog.setSleepHours(BigDecimal.ZERO);
        dailyLog.setExerciseMinutes(0);
        dailyLog.setEntertainmentMinutes(200);
        dailyLog.setMoodTag(null);
        dailyLog.setTaskCompletionRate(0);
        dailyLog.setRawText("晚上刷视频拖延，学习没有推进。");

        ScoreResult result = scoringService.calculate(dailyLog);

        assertThat(result.dailyScore()).isEqualByComparingTo("20.25");
        assertThat(result.rating()).isEqualTo("E");
        assertThat(result.growthScore()).isEqualByComparingTo("0.00");
        assertThat(result.executionScore()).isEqualByComparingTo("0.00");
        assertThat(result.energyScore()).isEqualByComparingTo("35.00");
        assertThat(result.moodScore()).isEqualByComparingTo("60.00");
        assertThat(result.distractionScore()).isEqualByComparingTo("20.00");
        assertThat(result.reflectionScore()).isEqualByComparingTo("40.00");
    }

    @Test
    void calculateUsesConservativeScoresWhenOptionalFieldsAreMissing() {
        DailyLogEntity dailyLog = dailyLog(7L, 103L, LocalDate.of(2026, 6, 11));
        dailyLog.setStudyHours(BigDecimal.valueOf(1));
        dailyLog.setWorkHours(BigDecimal.ZERO);
        dailyLog.setSleepHours(BigDecimal.valueOf(6));
        dailyLog.setExerciseMinutes(null);
        dailyLog.setEntertainmentMinutes(null);
        dailyLog.setTaskCompletionRate(null);
        dailyLog.setMoodTag(null);
        dailyLog.setCompletedContent("完成一组算法练习");
        dailyLog.setReflectionText(null);

        ScoreResult result = scoringService.calculate(dailyLog);

        assertThat(result.growthScore()).isEqualByComparingTo("25.00");
        assertThat(result.executionScore()).isEqualByComparingTo("50.00");
        assertThat(result.energyScore()).isEqualByComparingTo("80.00");
        assertThat(result.moodScore()).isEqualByComparingTo("60.00");
        assertThat(result.distractionScore()).isEqualByComparingTo("100.00");
        assertThat(result.reflectionScore()).isEqualByComparingTo("55.00");
    }

    @Test
    void calculateAndSaveUpsertsDailyScoreForUserAndDate() {
        DailyLogEntity dailyLog = dailyLog(7L, 104L, LocalDate.of(2026, 6, 12));
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setWorkHours(BigDecimal.valueOf(2));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(60);
        dailyLog.setEntertainmentMinutes(10);
        dailyLog.setMoodTag("平静");
        dailyLog.setTaskCompletionRate(100);
        dailyLog.setCompletedContent("完成主线任务");
        dailyLog.setReflectionText("今天执行力很好，睡眠和运动也比较稳定，后续继续保持。");

        DailyScoreEntity existing = new DailyScoreEntity();
        existing.setId(66L);
        when(dailyScoreRepository.findByUserIdAndLogDate(7L, dailyLog.getLogDate())).thenReturn(Optional.of(existing));
        when(dailyScoreRepository.save(any(DailyScoreEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyScoreEntity saved = scoringService.calculateAndSave(dailyLog);

        assertThat(saved.getId()).isEqualTo(66L);
        assertThat(saved.getUserId()).isEqualTo(7L);
        assertThat(saved.getDailyLogId()).isEqualTo(104L);
        assertThat(saved.getRating()).isEqualTo(DailyRating.S);
        assertThat(saved.getReasonJson()).contains("growthScore", "dailyScore");
        verify(dailyScoreRepository).save(existing);
    }

    private DailyLogEntity dailyLog(Long userId, Long dailyLogId, LocalDate logDate) {
        DailyLogEntity dailyLog = new DailyLogEntity();
        dailyLog.setId(dailyLogId);
        dailyLog.setUserId(userId);
        dailyLog.setLogDate(logDate);
        return dailyLog;
    }
}
