package com.lifequest.attribute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.attribute.entity.AttributeChangeEntity;
import com.lifequest.attribute.entity.UserAttributeEntity;
import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.scoring.model.ScoreResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AttributeServiceTests {

    private final UserAttributeRepository userAttributeRepository = org.mockito.Mockito.mock(UserAttributeRepository.class);
    private final AttributeChangeRepository attributeChangeRepository = org.mockito.Mockito.mock(AttributeChangeRepository.class);
    private final AttributeService attributeService = new AttributeService(
            userAttributeRepository,
            attributeChangeRepository,
            new ObjectMapper()
    );

    @Test
    void calculateReturnsAttributeDeltasAndReasonsForHealthyHighScoreDay() {
        DailyLogEntity dailyLog = dailyLog(7L, 101L, LocalDate.of(2026, 6, 9));
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        dailyLog.setTaskCompletionRate(90);

        AttributeChangeResult result = attributeService.calculate(dailyLog, highScore());

        assertThat(result.focusDelta()).isEqualTo(5);
        assertThat(result.disciplineDelta()).isEqualTo(4);
        assertThat(result.knowledgeDelta()).isEqualTo(7);
        assertThat(result.energyDelta()).isEqualTo(6);
        assertThat(result.moodDelta()).isEqualTo(4);
        assertThat(result.executionDelta()).isEqualTo(4);
        assertThat(result.balanceDelta()).isEqualTo(4);
        assertThat(result.expDelta()).isEqualTo(96);
        assertThat(result.reasons()).containsKeys(
                "focus",
                "discipline",
                "knowledge",
                "energy",
                "mood",
                "execution",
                "balance",
                "exp"
        );
    }

    @Test
    void calculateReturnsNegativeDeltasForLowScoreAndUnbalancedDay() {
        DailyLogEntity dailyLog = dailyLog(7L, 102L, LocalDate.of(2026, 6, 10));
        dailyLog.setStudyHours(BigDecimal.ZERO);
        dailyLog.setSleepHours(BigDecimal.valueOf(5));
        dailyLog.setExerciseMinutes(0);
        dailyLog.setEntertainmentMinutes(200);
        dailyLog.setTaskCompletionRate(0);

        AttributeChangeResult result = attributeService.calculate(dailyLog, lowScore());

        assertThat(result.focusDelta()).isEqualTo(-4);
        assertThat(result.disciplineDelta()).isEqualTo(-3);
        assertThat(result.knowledgeDelta()).isEqualTo(-1);
        assertThat(result.energyDelta()).isEqualTo(-4);
        assertThat(result.moodDelta()).isEqualTo(-2);
        assertThat(result.executionDelta()).isEqualTo(-2);
        assertThat(result.balanceDelta()).isEqualTo(-6);
        assertThat(result.expDelta()).isEqualTo(20);
    }

    @Test
    void calculateAndSaveUpdatesUserAttributeAndCreatesChangeRecord() {
        DailyLogEntity dailyLog = dailyLog(7L, 103L, LocalDate.of(2026, 6, 11));
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        dailyLog.setTaskCompletionRate(90);
        UserAttributeEntity attribute = new UserAttributeEntity();
        attribute.setUserId(7L);
        when(userAttributeRepository.findByUserId(7L)).thenReturn(Optional.of(attribute));
        when(attributeChangeRepository.findByUserIdAndLogDate(7L, dailyLog.getLogDate())).thenReturn(Optional.empty());
        when(attributeChangeRepository.save(any(AttributeChangeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttributeChangeEntity change = attributeService.calculateAndSave(dailyLog, highScore());

        assertThat(attribute.getFocus()).isEqualTo(55);
        assertThat(attribute.getDiscipline()).isEqualTo(54);
        assertThat(attribute.getKnowledge()).isEqualTo(57);
        assertThat(attribute.getEnergy()).isEqualTo(56);
        assertThat(attribute.getMood()).isEqualTo(54);
        assertThat(attribute.getExecution()).isEqualTo(54);
        assertThat(attribute.getBalance()).isEqualTo(54);
        assertThat(attribute.getTotalExp()).isEqualTo(96);
        assertThat(attribute.getLevel()).isEqualTo(1);
        assertThat(attribute.getExp()).isEqualTo(96);
        assertThat(change.getUserId()).isEqualTo(7L);
        assertThat(change.getDailyLogId()).isEqualTo(103L);
        assertThat(change.getReasonJson()).contains("focus", "exp");
        verify(userAttributeRepository).save(attribute);
        verify(attributeChangeRepository).save(change);
    }

    @Test
    void calculateAndSaveRollsBackPreviousSameDayChangeBeforeApplyingNewChange() {
        DailyLogEntity dailyLog = dailyLog(7L, 104L, LocalDate.of(2026, 6, 12));
        dailyLog.setStudyHours(BigDecimal.valueOf(4));
        dailyLog.setSleepHours(BigDecimal.valueOf(8));
        dailyLog.setExerciseMinutes(30);
        dailyLog.setEntertainmentMinutes(20);
        dailyLog.setTaskCompletionRate(90);
        UserAttributeEntity attribute = new UserAttributeEntity();
        attribute.setUserId(7L);
        attribute.setFocus(52);
        attribute.setDiscipline(51);
        attribute.setKnowledge(53);
        attribute.setEnergy(50);
        attribute.setMood(50);
        attribute.setExecution(51);
        attribute.setBalance(50);
        attribute.setTotalExp(180);
        attribute.setLevel(2);
        attribute.setExp(80);
        AttributeChangeEntity previous = new AttributeChangeEntity();
        previous.setUserId(7L);
        previous.setFocusDelta(2);
        previous.setDisciplineDelta(1);
        previous.setKnowledgeDelta(3);
        previous.setEnergyDelta(0);
        previous.setMoodDelta(0);
        previous.setExecutionDelta(1);
        previous.setBalanceDelta(0);
        previous.setExpDelta(80);
        when(userAttributeRepository.findByUserId(7L)).thenReturn(Optional.of(attribute));
        when(attributeChangeRepository.findByUserIdAndLogDate(7L, dailyLog.getLogDate())).thenReturn(Optional.of(previous));
        when(attributeChangeRepository.save(any(AttributeChangeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttributeChangeEntity change = attributeService.calculateAndSave(dailyLog, highScore());

        assertThat(attribute.getFocus()).isEqualTo(55);
        assertThat(attribute.getDiscipline()).isEqualTo(54);
        assertThat(attribute.getKnowledge()).isEqualTo(57);
        assertThat(attribute.getTotalExp()).isEqualTo(196);
        assertThat(attribute.getLevel()).isEqualTo(2);
        assertThat(attribute.getExp()).isEqualTo(96);
        assertThat(change).isSameAs(previous);
        assertThat(change.getFocusDelta()).isEqualTo(5);
        assertThat(change.getExpDelta()).isEqualTo(96);
    }

    private DailyLogEntity dailyLog(Long userId, Long dailyLogId, LocalDate logDate) {
        DailyLogEntity dailyLog = new DailyLogEntity();
        dailyLog.setId(dailyLogId);
        dailyLog.setUserId(userId);
        dailyLog.setLogDate(logDate);
        return dailyLog;
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
