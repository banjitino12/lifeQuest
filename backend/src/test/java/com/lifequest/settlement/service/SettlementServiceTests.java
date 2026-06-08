package com.lifequest.settlement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.attribute.entity.AttributeChangeEntity;
import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.service.AttributeService;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.enums.DailyLogSourceType;
import com.lifequest.common.enums.DailyRating;
import com.lifequest.common.enums.GameEventType;
import com.lifequest.dailylog.dto.DailyLogSubmitResult;
import com.lifequest.dailylog.dto.SubmitDailyLogRequest;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.dailylog.service.DailyLogService;
import com.lifequest.gamification.entity.GameEventEntity;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.gamification.service.GamificationService;
import com.lifequest.scoring.entity.DailyScoreEntity;
import com.lifequest.scoring.model.ScoreResult;
import com.lifequest.scoring.repository.DailyScoreRepository;
import com.lifequest.scoring.service.ScoringService;
import com.lifequest.settlement.dto.SettlementResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class SettlementServiceTests {

    private final CurrentUserService currentUserService = org.mockito.Mockito.mock(CurrentUserService.class);
    private final DailyLogService dailyLogService = org.mockito.Mockito.mock(DailyLogService.class);
    private final DailyLogRepository dailyLogRepository = org.mockito.Mockito.mock(DailyLogRepository.class);
    private final ScoringService scoringService = org.mockito.Mockito.mock(ScoringService.class);
    private final DailyScoreRepository dailyScoreRepository = org.mockito.Mockito.mock(DailyScoreRepository.class);
    private final AttributeService attributeService = org.mockito.Mockito.mock(AttributeService.class);
    private final AttributeChangeRepository attributeChangeRepository = org.mockito.Mockito.mock(AttributeChangeRepository.class);
    private final GamificationService gamificationService = org.mockito.Mockito.mock(GamificationService.class);
    private final GameEventRepository gameEventRepository = org.mockito.Mockito.mock(GameEventRepository.class);
    private final SettlementService settlementService = new SettlementService(
            currentUserService,
            dailyLogService,
            dailyLogRepository,
            scoringService,
            dailyScoreRepository,
            attributeService,
            attributeChangeRepository,
            gamificationService,
            gameEventRepository,
            new ObjectMapper()
    );

    @Test
    void submitAndSettleOrchestratesCoreServicesAndReturnsFallbackWhenLlmFails() {
        LocalDate logDate = LocalDate.of(2026, 6, 9);
        SubmitDailyLogRequest request = request(logDate);
        DailyLogEntity dailyLog = dailyLog(logDate);
        ScoreResult score = score();
        AttributeChangeResult attributeChange = attributeChange();
        GameEventEntity event = event("DAILY_SETTLEMENT", "每日结算", GameEventType.STORY);
        when(dailyLogService.submitAndReturnLog(request))
                .thenReturn(new DailyLogSubmitResult(dailyLog, "MIXED", "FAILED", false));
        when(scoringService.calculate(dailyLog)).thenReturn(score);
        when(attributeService.calculate(dailyLog, score)).thenReturn(attributeChange);
        when(gamificationService.generateAndSaveEvents(dailyLog, score, attributeChange)).thenReturn(List.of(event));

        SettlementResponse response = settlementService.submitAndSettle(request);

        assertThat(response.dailyLogId()).isEqualTo(101L);
        assertThat(response.score().rating()).isEqualTo("B");
        assertThat(response.attributeChange().expDelta()).isEqualTo(78);
        assertThat(response.events()).extracting(SettlementResponse.GameEventBlock::eventCode).containsExactly("DAILY_SETTLEMENT");
        assertThat(response.llm().status()).isEqualTo("FAILED");
        assertThat(response.llm().fallbackUsed()).isTrue();
        assertThat(response.tomorrowTasks()).isEmpty();
        assertThat(response.basicSuggestion()).isNotBlank();

        InOrder order = inOrder(dailyLogService, scoringService, attributeService, gamificationService);
        order.verify(dailyLogService).submitAndReturnLog(request);
        order.verify(scoringService).calculate(dailyLog);
        order.verify(scoringService).calculateAndSave(dailyLog);
        order.verify(attributeService).calculate(dailyLog, score);
        order.verify(attributeService).calculateAndSave(dailyLog, score);
        order.verify(gamificationService).generateAndSaveEvents(dailyLog, score, attributeChange);
    }

    @Test
    void getByDateReturnsPersistedSettlementForCurrentUser() {
        LocalDate logDate = LocalDate.of(2026, 6, 9);
        DailyLogEntity dailyLog = dailyLog(logDate);
        dailyLog.setParsedJson("{\"status\":\"SUCCESS\"}");
        DailyScoreEntity dailyScore = dailyScore(logDate);
        AttributeChangeEntity attributeChange = attributeChangeEntity(logDate);
        GameEventEntity event = event("DEEP_FOCUS", "深度专注", GameEventType.BUFF);
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(dailyLogRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.of(dailyLog));
        when(dailyScoreRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.of(dailyScore));
        when(attributeChangeRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.of(attributeChange));
        when(gameEventRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(List.of(event));

        SettlementResponse response = settlementService.getByDate(logDate);

        assertThat(response.dailyLogId()).isEqualTo(101L);
        assertThat(response.score().dailyScore()).isEqualByComparingTo("78.50");
        assertThat(response.score().reasons()).containsEntry("dailyScore", "weighted");
        assertThat(response.attributeChange().focusDelta()).isEqualTo(2);
        assertThat(response.attributeChange().reasons()).containsEntry("focus", "focused");
        assertThat(response.events()).extracting(SettlementResponse.GameEventBlock::eventCode).containsExactly("DEEP_FOCUS");
        assertThat(response.llm().status()).isEqualTo("SUCCESS");
        assertThat(response.llm().fallbackUsed()).isFalse();
    }

    private SubmitDailyLogRequest request(LocalDate logDate) {
        return new SubmitDailyLogRequest(
                logDate,
                "今天学习 Redis，晚上有点分心",
                3.5,
                1.0,
                6.5,
                20,
                90,
                "焦虑",
                75,
                "复习 Redis",
                "晚上分心",
                "明天提前收尾",
                "MIXED"
        );
    }

    private DailyLogEntity dailyLog(LocalDate logDate) {
        DailyLogEntity dailyLog = new DailyLogEntity();
        dailyLog.setId(101L);
        dailyLog.setUserId(7L);
        dailyLog.setLogDate(logDate);
        dailyLog.setSourceType(DailyLogSourceType.MIXED);
        return dailyLog;
    }

    private ScoreResult score() {
        return new ScoreResult(
                BigDecimal.valueOf(78.5),
                "B",
                BigDecimal.valueOf(82),
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(68),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(65),
                BigDecimal.valueOf(85),
                Map.of("dailyScore", "weighted")
        );
    }

    private AttributeChangeResult attributeChange() {
        return new AttributeChangeResult(2, 1, 5, -1, 0, 3, -1, 78, Map.of("focus", "focused"));
    }

    private DailyScoreEntity dailyScore(LocalDate logDate) {
        DailyScoreEntity score = new DailyScoreEntity();
        score.setUserId(7L);
        score.setDailyLogId(101L);
        score.setLogDate(logDate);
        score.setDailyScore(BigDecimal.valueOf(78.5));
        score.setRating(DailyRating.B);
        score.setGrowthScore(BigDecimal.valueOf(82));
        score.setExecutionScore(BigDecimal.valueOf(75));
        score.setEnergyScore(BigDecimal.valueOf(68));
        score.setMoodScore(BigDecimal.valueOf(70));
        score.setDistractionScore(BigDecimal.valueOf(65));
        score.setReflectionScore(BigDecimal.valueOf(85));
        score.setReasonJson("{\"dailyScore\":\"weighted\"}");
        return score;
    }

    private AttributeChangeEntity attributeChangeEntity(LocalDate logDate) {
        AttributeChangeEntity change = new AttributeChangeEntity();
        change.setUserId(7L);
        change.setDailyLogId(101L);
        change.setLogDate(logDate);
        change.setFocusDelta(2);
        change.setDisciplineDelta(1);
        change.setKnowledgeDelta(5);
        change.setEnergyDelta(-1);
        change.setMoodDelta(0);
        change.setExecutionDelta(3);
        change.setBalanceDelta(-1);
        change.setExpDelta(78);
        change.setReasonJson("{\"focus\":\"focused\"}");
        return change;
    }

    private GameEventEntity event(String code, String name, GameEventType type) {
        GameEventEntity event = new GameEventEntity();
        event.setUserId(7L);
        event.setDailyLogId(101L);
        event.setLogDate(LocalDate.of(2026, 6, 9));
        event.setEventType(type);
        event.setEventCode(code);
        event.setEventName(name);
        event.setEventLevel(1);
        event.setEventDescription(name);
        event.setEffectJson("{}");
        return event;
    }
}
