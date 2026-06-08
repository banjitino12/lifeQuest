package com.lifequest.attribute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.attribute.entity.AttributeChangeEntity;
import com.lifequest.attribute.entity.UserAttributeEntity;
import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.attribute.model.AttributeSnapshot;
import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.scoring.model.ScoreResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributeService {

    private static final int ATTRIBUTE_MIN = 0;
    private static final int ATTRIBUTE_MAX = 100;
    private static final int EXP_PER_LEVEL = 100;

    private final UserAttributeRepository userAttributeRepository;
    private final AttributeChangeRepository attributeChangeRepository;
    private final ObjectMapper objectMapper;

    public AttributeService(
            UserAttributeRepository userAttributeRepository,
            AttributeChangeRepository attributeChangeRepository,
            ObjectMapper objectMapper
    ) {
        this.userAttributeRepository = userAttributeRepository;
        this.attributeChangeRepository = attributeChangeRepository;
        this.objectMapper = objectMapper;
    }

    public AttributeSnapshot initialSnapshot() {
        return new AttributeSnapshot(50, 50, 50, 50, 50, 50, 50, 1, 0);
    }

    public AttributeChangeResult emptyChange() {
        return new AttributeChangeResult(0, 0, 0, 0, 0, 0, 0, 0, Map.of());
    }

    public AttributeChangeResult calculate(DailyLogEntity dailyLog, ScoreResult scoreResult) {
        LinkedHashMap<String, String> reasons = new LinkedHashMap<>();

        int focusDelta = clampDelta(scoreDelta(scoreResult.distractionScore())
                + (hours(dailyLog.getStudyHours()) >= 4 ? 2 : hours(dailyLog.getStudyHours()) >= 2 ? 1 : 0)
                - (minutes(dailyLog.getEntertainmentMinutes()) >= 180 ? 2 : minutes(dailyLog.getEntertainmentMinutes()) >= 120 ? 1 : 0));
        reasons.put("focus", String.format(
                "专注力由娱乐控制分、学习时长和娱乐时长共同决定：学习 %.1f 小时，娱乐 %d 分钟。",
                hours(dailyLog.getStudyHours()),
                minutes(dailyLog.getEntertainmentMinutes())
        ));

        int disciplineDelta = clampDelta(scoreDelta(scoreResult.executionScore())
                + (rate(dailyLog.getTaskCompletionRate()) >= 80 ? 1 : 0)
                - (minutes(dailyLog.getEntertainmentMinutes()) >= 180 ? 1 : 0));
        reasons.put("discipline", "自律由任务执行分、任务完成率和娱乐失控风险共同决定。");

        int knowledgeDelta = clampDelta((int) Math.round(hours(dailyLog.getStudyHours()) * 1.5)
                + (scoreResult.growthScore().compareTo(BigDecimal.valueOf(80)) >= 0 ? 1 : 0)
                - (scoreResult.growthScore().compareTo(BigDecimal.valueOf(40)) < 0 ? 1 : 0));
        reasons.put("knowledge", String.format(
                "知识积累主要来自学习时长和学习成长得分：学习 %.1f 小时，成长分 %s。",
                hours(dailyLog.getStudyHours()),
                scoreResult.growthScore()
        ));

        int energyDelta = clampDelta(scoreDelta(scoreResult.energyScore())
                + (minutes(dailyLog.getExerciseMinutes()) >= 30 ? 2 : 0)
                + (hours(dailyLog.getSleepHours()) >= 8 ? 1 : 0)
                - (hours(dailyLog.getSleepHours()) > 0 && hours(dailyLog.getSleepHours()) < 6 ? 2 : 0));
        reasons.put("energy", String.format(
                "精力由精力恢复分、睡眠时长和运动时长共同决定：睡眠 %.1f 小时，运动 %d 分钟。",
                hours(dailyLog.getSleepHours()),
                minutes(dailyLog.getExerciseMinutes())
        ));

        int moodDelta = clampDelta(scoreDelta(scoreResult.moodScore())
                + (minutes(dailyLog.getExerciseMinutes()) >= 30 ? 1 : 0)
                - (scoreResult.moodScore().compareTo(BigDecimal.valueOf(50)) < 0 ? 1 : 0));
        reasons.put("mood", "情绪稳定由情绪状态分和运动恢复共同决定。");

        int executionDelta = clampDelta(scoreDelta(scoreResult.executionScore())
                + (rate(dailyLog.getTaskCompletionRate()) >= 90 ? 1 : 0));
        reasons.put("execution", "执行力由任务执行分和高完成率加成共同决定。");

        BigDecimal balanceScore = scoreResult.energyScore().add(scoreResult.distractionScore())
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        int balanceDelta = clampDelta(scoreDelta(balanceScore)
                + (minutes(dailyLog.getExerciseMinutes()) >= 30 ? 1 : 0)
                - (hours(dailyLog.getSleepHours()) > 0 && hours(dailyLog.getSleepHours()) < 6 ? 2 : 0)
                - (minutes(dailyLog.getEntertainmentMinutes()) >= 180 ? 2 : 0));
        reasons.put("balance", "生活平衡由精力恢复、娱乐控制、运动、睡眠不足和娱乐失控共同决定。");

        int expDelta = Math.max(10, scoreResult.dailyScore().setScale(0, RoundingMode.HALF_UP).intValue());
        reasons.put("exp", "经验值由每日总分换算，低分日也保留最低 10 点参与经验。");

        return new AttributeChangeResult(
                focusDelta,
                disciplineDelta,
                knowledgeDelta,
                energyDelta,
                moodDelta,
                executionDelta,
                balanceDelta,
                expDelta,
                reasons
        );
    }

    @Transactional
    public AttributeChangeEntity calculateAndSave(DailyLogEntity dailyLog, ScoreResult scoreResult) {
        AttributeChangeResult result = calculate(dailyLog, scoreResult);
        UserAttributeEntity attribute = userAttributeRepository.findByUserId(dailyLog.getUserId())
                .orElseGet(() -> initialAttribute(dailyLog.getUserId()));
        AttributeChangeEntity change = attributeChangeRepository
                .findByUserIdAndLogDate(dailyLog.getUserId(), dailyLog.getLogDate())
                .orElseGet(AttributeChangeEntity::new);

        rollbackPreviousChange(attribute, change);
        applyChange(attribute, result);

        change.setUserId(dailyLog.getUserId());
        change.setDailyLogId(dailyLog.getId());
        change.setLogDate(dailyLog.getLogDate());
        change.setFocusDelta(result.focusDelta());
        change.setDisciplineDelta(result.disciplineDelta());
        change.setKnowledgeDelta(result.knowledgeDelta());
        change.setEnergyDelta(result.energyDelta());
        change.setMoodDelta(result.moodDelta());
        change.setExecutionDelta(result.executionDelta());
        change.setBalanceDelta(result.balanceDelta());
        change.setExpDelta(result.expDelta());
        change.setReasonJson(toJson(result.reasons()));

        userAttributeRepository.save(attribute);
        return attributeChangeRepository.save(change);
    }

    private UserAttributeEntity initialAttribute(Long userId) {
        UserAttributeEntity attribute = new UserAttributeEntity();
        attribute.setUserId(userId);
        return attribute;
    }

    private void rollbackPreviousChange(UserAttributeEntity attribute, AttributeChangeEntity previousChange) {
        if (previousChange.getUserId() == null) {
            return;
        }
        attribute.setFocus(clampAttribute(attribute.getFocus() - previousChange.getFocusDelta()));
        attribute.setDiscipline(clampAttribute(attribute.getDiscipline() - previousChange.getDisciplineDelta()));
        attribute.setKnowledge(clampAttribute(attribute.getKnowledge() - previousChange.getKnowledgeDelta()));
        attribute.setEnergy(clampAttribute(attribute.getEnergy() - previousChange.getEnergyDelta()));
        attribute.setMood(clampAttribute(attribute.getMood() - previousChange.getMoodDelta()));
        attribute.setExecution(clampAttribute(attribute.getExecution() - previousChange.getExecutionDelta()));
        attribute.setBalance(clampAttribute(attribute.getBalance() - previousChange.getBalanceDelta()));
        int totalExp = Math.max(0, attribute.getTotalExp() - previousChange.getExpDelta());
        updateExperience(attribute, totalExp);
    }

    private void applyChange(UserAttributeEntity attribute, AttributeChangeResult result) {
        attribute.setFocus(clampAttribute(attribute.getFocus() + result.focusDelta()));
        attribute.setDiscipline(clampAttribute(attribute.getDiscipline() + result.disciplineDelta()));
        attribute.setKnowledge(clampAttribute(attribute.getKnowledge() + result.knowledgeDelta()));
        attribute.setEnergy(clampAttribute(attribute.getEnergy() + result.energyDelta()));
        attribute.setMood(clampAttribute(attribute.getMood() + result.moodDelta()));
        attribute.setExecution(clampAttribute(attribute.getExecution() + result.executionDelta()));
        attribute.setBalance(clampAttribute(attribute.getBalance() + result.balanceDelta()));
        updateExperience(attribute, attribute.getTotalExp() + result.expDelta());
    }

    private void updateExperience(UserAttributeEntity attribute, int totalExp) {
        attribute.setTotalExp(totalExp);
        attribute.setLevel(totalExp / EXP_PER_LEVEL + 1);
        attribute.setExp(totalExp % EXP_PER_LEVEL);
    }

    private int scoreDelta(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return 3;
        }
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return 2;
        }
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return 1;
        }
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return 0;
        }
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return -1;
        }
        return -2;
    }

    private int clampDelta(int value) {
        return Math.max(-8, Math.min(8, value));
    }

    private int clampAttribute(int value) {
        return Math.max(ATTRIBUTE_MIN, Math.min(ATTRIBUTE_MAX, value));
    }

    private double hours(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private int minutes(Integer value) {
        return value == null ? 0 : value;
    }

    private int rate(Integer value) {
        return value == null ? 0 : value;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "属性变化原因序列化失败");
        }
    }
}
