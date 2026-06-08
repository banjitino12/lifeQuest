package com.lifequest.gamification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.gamification.entity.GameEventEntity;
import com.lifequest.gamification.model.GameEventResult;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.scoring.model.ScoreResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GamificationServiceTests {

    private final GameEventRepository gameEventRepository = org.mockito.Mockito.mock(GameEventRepository.class);
    private final GamificationService gamificationService = new GamificationService(gameEventRepository);

    @Test
    void generateEventsCreatesPositiveBuffsFromStructuredBehavior() {
        DailyLogEntity dailyLog = dailyLog();
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        dailyLog.setReflectionText("今天复盘比较完整，明确了学习推进、分心风险和明天的主要任务安排。");

        List<GameEventResult> events = gamificationService.generateEvents(dailyLog, highScore(), attributeChange(96));

        assertThat(events).extracting(GameEventResult::eventCode)
                .contains("DEEP_FOCUS", "REFLECTION_INSIGHT", "EXERCISE_RECOVERY", "HIGH_RANK_CLEAR", "DAILY_SETTLEMENT");
        assertThat(events).filteredOn(event -> event.eventCode().equals("DEEP_FOCUS"))
                .singleElement()
                .extracting(GameEventResult::eventLevel)
                .isEqualTo(2);
    }

    @Test
    void generateEventsCreatesDebuffsAndEnemiesForSleepDebtAndDistraction() {
        DailyLogEntity dailyLog = dailyLog();
        dailyLog.setStudyHours(BigDecimal.ZERO);
        dailyLog.setSleepHours(BigDecimal.valueOf(5));
        dailyLog.setExerciseMinutes(0);
        dailyLog.setEntertainmentMinutes(200);
        dailyLog.setRawText("晚上刷视频拖延，学习没有推进。");
        dailyLog.setProblemText("晚间分心严重");

        List<GameEventResult> events = gamificationService.generateEvents(dailyLog, lowScore(), attributeChange(20));

        assertThat(events).extracting(GameEventResult::eventCode)
                .contains(
                        "SLEEP_DEBT",
                        "LATE_NIGHT_GHOST",
                        "EVENING_DISTRACTION",
                        "SHORT_VIDEO_TEMPTATION",
                        "ANXIETY_FOG",
                        "PROCRASTINATION_SLIME",
                        "DAILY_SETTLEMENT"
                );
        assertThat(events).filteredOn(event -> event.eventCode().equals("SHORT_VIDEO_TEMPTATION"))
                .singleElement()
                .extracting(GameEventResult::eventType)
                .isEqualTo("ENEMY");
    }

    @Test
    void generateAndSaveEventsDeletesOldDailyEventsBeforeSavingNewOnes() {
        DailyLogEntity dailyLog = dailyLog();
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        GameEventEntity oldEvent = new GameEventEntity();
        oldEvent.setUserId(7L);
        oldEvent.setDailyLogId(101L);
        when(gameEventRepository.findByUserIdAndLogDate(7L, dailyLog.getLogDate())).thenReturn(List.of(oldEvent));
        when(gameEventRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<GameEventEntity> savedEvents = gamificationService.generateAndSaveEvents(dailyLog, highScore(), attributeChange(96));

        verify(gameEventRepository).deleteAll(List.of(oldEvent));
        verify(gameEventRepository).saveAll(anyList());
        assertThat(savedEvents).extracting(GameEventEntity::getUserId).containsOnly(7L);
        assertThat(savedEvents).extracting(GameEventEntity::getDailyLogId).containsOnly(101L);
        assertThat(savedEvents).extracting(GameEventEntity::getEventCode).contains("DEEP_FOCUS", "EXERCISE_RECOVERY", "DAILY_SETTLEMENT");
    }

    private DailyLogEntity dailyLog() {
        DailyLogEntity dailyLog = new DailyLogEntity();
        dailyLog.setId(101L);
        dailyLog.setUserId(7L);
        dailyLog.setLogDate(LocalDate.of(2026, 6, 9));
        return dailyLog;
    }

    private AttributeChangeResult attributeChange(int expDelta) {
        return new AttributeChangeResult(0, 0, 0, 0, 0, 0, 0, expDelta, Map.of());
    }

    private ScoreResult highScore() {
        return new ScoreResult(
                BigDecimal.valueOf(96),
                "S",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                Map.of()
        );
    }

    private ScoreResult lowScore() {
        return new ScoreResult(
                BigDecimal.valueOf(20.25),
                "E",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(35),
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(40),
                Map.of()
        );
    }
}
