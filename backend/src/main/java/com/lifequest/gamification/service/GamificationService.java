package com.lifequest.gamification.service;

import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.common.enums.GameEventType;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.gamification.entity.GameEventEntity;
import com.lifequest.gamification.model.GameEventResult;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.scoring.model.ScoreResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GamificationService {

    private final GameEventRepository gameEventRepository;

    public GamificationService(GameEventRepository gameEventRepository) {
        this.gameEventRepository = gameEventRepository;
    }

    public List<GameEventResult> generateEvents(
            DailyLogEntity dailyLog,
            ScoreResult scoreResult,
            AttributeChangeResult attributeChange
    ) {
        List<GameEventResult> events = new ArrayList<>();
        addPositiveEvents(events, dailyLog, scoreResult, attributeChange);
        addNegativeEvents(events, dailyLog, scoreResult);
        events.add(settlementEvent(scoreResult, attributeChange));
        return events;
    }

    @Transactional
    public List<GameEventEntity> generateAndSaveEvents(
            DailyLogEntity dailyLog,
            ScoreResult scoreResult,
            AttributeChangeResult attributeChange
    ) {
        List<GameEventEntity> oldEvents = gameEventRepository.findByUserIdAndLogDate(
                dailyLog.getUserId(),
                dailyLog.getLogDate()
        );
        gameEventRepository.deleteAll(oldEvents);

        List<GameEventEntity> entities = generateEvents(dailyLog, scoreResult, attributeChange)
                .stream()
                .map(result -> toEntity(dailyLog, result))
                .toList();
        return gameEventRepository.saveAll(entities);
    }

    private void addPositiveEvents(
            List<GameEventResult> events,
            DailyLogEntity dailyLog,
            ScoreResult scoreResult,
            AttributeChangeResult attributeChange
    ) {
        if (hours(dailyLog.getStudyHours()) >= 1.5 || scoreResult.growthScore().compareTo(BigDecimal.valueOf(90)) >= 0) {
            events.add(event(
                    GameEventType.BUFF,
                    "DEEP_FOCUS",
                    "深度专注",
                    levelByStudyHours(dailyLog),
                    String.format("学习 %.1f 小时，触发稳定专注状态。", hours(dailyLog.getStudyHours())),
                    "{\"focus\":2,\"knowledge\":2}"
            ));
        }
        if (textLength(dailyLog.getReflectionText()) >= 40 || scoreResult.reflectionScore().compareTo(BigDecimal.valueOf(85)) >= 0) {
            events.add(event(
                    GameEventType.BUFF,
                    "REFLECTION_INSIGHT",
                    "复盘洞察",
                    1,
                    "完成了较高质量复盘，今天的经验被转化为可复用洞察。",
                    "{\"execution\":1,\"discipline\":1}"
            ));
        }
        if (minutes(dailyLog.getExerciseMinutes()) >= 30) {
            events.add(event(
                    GameEventType.BUFF,
                    "EXERCISE_RECOVERY",
                    "运动恢复",
                    minutes(dailyLog.getExerciseMinutes()) >= 60 ? 2 : 1,
                    String.format("运动 %d 分钟，帮助恢复精力和情绪稳定。", minutes(dailyLog.getExerciseMinutes())),
                    "{\"energy\":2,\"mood\":1}"
            ));
        }
        if (scoreResult.dailyScore().compareTo(BigDecimal.valueOf(90)) >= 0
                && attributeChange.expDelta() >= 90) {
            events.add(event(
                    GameEventType.ACHIEVEMENT,
                    "HIGH_RANK_CLEAR",
                    "高评级通关",
                    1,
                    "今日评分达到 S 级，完成了一次高质量成长结算。",
                    "{\"expBonus\":0}"
            ));
        }
    }

    private void addNegativeEvents(List<GameEventResult> events, DailyLogEntity dailyLog, ScoreResult scoreResult) {
        if (hours(dailyLog.getSleepHours()) > 0 && hours(dailyLog.getSleepHours()) < 6) {
            events.add(event(
                    GameEventType.DEBUFF,
                    "SLEEP_DEBT",
                    "睡眠不足",
                    hours(dailyLog.getSleepHours()) < 4 ? 2 : 1,
                    String.format("睡眠 %.1f 小时，恢复不足会影响明天的精力和专注。", hours(dailyLog.getSleepHours())),
                    "{\"energy\":-2,\"balance\":-1}"
            ));
            events.add(event(
                    GameEventType.ENEMY,
                    "LATE_NIGHT_GHOST",
                    "熬夜幽灵",
                    hours(dailyLog.getSleepHours()) < 4 ? 2 : 1,
                    "睡眠不足被外化为需要处理的作息敌人。",
                    "{\"risk\":\"sleep\"}"
            ));
        }
        if (isDistracted(dailyLog, scoreResult)) {
            int level = minutes(dailyLog.getEntertainmentMinutes()) >= 180 ? 2 : 1;
            events.add(event(
                    GameEventType.DEBUFF,
                    "EVENING_DISTRACTION",
                    "晚间分心",
                    level,
                    "娱乐时间偏高或日志出现分心/拖延迹象，晚间节奏受到干扰。",
                    "{\"focus\":-2,\"discipline\":-1}"
            ));
            events.add(event(
                    GameEventType.ENEMY,
                    "SHORT_VIDEO_TEMPTATION",
                    "短视频魅魔",
                    level,
                    "娱乐失控和任务推进不足被外化为今日主要敌人。",
                    "{\"risk\":\"entertainment\"}"
            ));
        }
        if (scoreResult.moodScore().compareTo(BigDecimal.valueOf(50)) < 0) {
            events.add(event(
                    GameEventType.ENEMY,
                    "ANXIETY_FOG",
                    "焦虑雾兽",
                    1,
                    "情绪状态偏低，需要降低明日任务压力并恢复节奏。",
                    "{\"risk\":\"mood\"}"
            ));
        }
        if (scoreResult.executionScore().compareTo(BigDecimal.valueOf(40)) < 0
                && hours(dailyLog.getStudyHours()) < 1) {
            events.add(event(
                    GameEventType.ENEMY,
                    "PROCRASTINATION_SLIME",
                    "拖延史莱姆",
                    1,
                    "任务执行和学习推进都偏弱，拖延风险被标记为今日敌人。",
                    "{\"risk\":\"execution\"}"
            ));
        }
    }

    private boolean isDistracted(DailyLogEntity dailyLog, ScoreResult scoreResult) {
        return minutes(dailyLog.getEntertainmentMinutes()) >= 120
                || scoreResult.distractionScore().compareTo(BigDecimal.valueOf(60)) < 0
                || containsAny(dailyLog.getRawText(), "分心", "刷视频", "拖延")
                || containsAny(dailyLog.getProblemText(), "分心", "刷视频", "拖延");
    }

    private GameEventResult settlementEvent(ScoreResult scoreResult, AttributeChangeResult attributeChange) {
        return event(
                GameEventType.STORY,
                "DAILY_SETTLEMENT",
                "每日结算",
                1,
                String.format("今日评级 %s，获得 %d 点经验，系统已完成基础成长结算。", scoreResult.rating(), attributeChange.expDelta()),
                "{\"settlement\":true}"
        );
    }

    private GameEventEntity toEntity(DailyLogEntity dailyLog, GameEventResult result) {
        GameEventEntity entity = new GameEventEntity();
        entity.setUserId(dailyLog.getUserId());
        entity.setDailyLogId(dailyLog.getId());
        entity.setLogDate(dailyLog.getLogDate());
        entity.setEventType(GameEventType.valueOf(result.eventType()));
        entity.setEventCode(result.eventCode());
        entity.setEventName(result.eventName());
        entity.setEventLevel(result.eventLevel());
        entity.setEventDescription(result.eventDescription());
        entity.setEffectJson(result.effectJson());
        return entity;
    }

    private GameEventResult event(
            GameEventType eventType,
            String eventCode,
            String eventName,
            Integer eventLevel,
            String eventDescription,
            String effectJson
    ) {
        return new GameEventResult(eventType.name(), eventCode, eventName, eventLevel, eventDescription, effectJson);
    }

    private int levelByStudyHours(DailyLogEntity dailyLog) {
        return hours(dailyLog.getStudyHours()) >= 4 ? 2 : 1;
    }

    private double hours(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private int minutes(Integer value) {
        return value == null ? 0 : value;
    }

    private int textLength(String value) {
        return StringUtils.hasText(value) ? value.trim().length() : 0;
    }

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
