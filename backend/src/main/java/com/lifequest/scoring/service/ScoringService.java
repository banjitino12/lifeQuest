package com.lifequest.scoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.common.enums.DailyRating;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.scoring.entity.DailyScoreEntity;
import com.lifequest.scoring.model.ScoreResult;
import com.lifequest.scoring.repository.DailyScoreRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ScoringService {

    private static final BigDecimal GROWTH_WEIGHT = BigDecimal.valueOf(0.25);
    private static final BigDecimal EXECUTION_WEIGHT = BigDecimal.valueOf(0.25);
    private static final BigDecimal ENERGY_WEIGHT = BigDecimal.valueOf(0.15);
    private static final BigDecimal MOOD_WEIGHT = BigDecimal.valueOf(0.15);
    private static final BigDecimal DISTRACTION_WEIGHT = BigDecimal.valueOf(0.10);
    private static final BigDecimal REFLECTION_WEIGHT = BigDecimal.valueOf(0.10);

    private final DailyScoreRepository dailyScoreRepository;
    private final ObjectMapper objectMapper;

    public ScoringService(DailyScoreRepository dailyScoreRepository, ObjectMapper objectMapper) {
        this.dailyScoreRepository = dailyScoreRepository;
        this.objectMapper = objectMapper;
    }

    public ScoreResult calculate(DailyLogEntity dailyLog) {
        LinkedHashMap<String, String> reasons = new LinkedHashMap<>();

        BigDecimal growthScore = scoreGrowth(dailyLog, reasons);
        BigDecimal executionScore = scoreExecution(dailyLog, reasons);
        BigDecimal energyScore = scoreEnergy(dailyLog, reasons);
        BigDecimal moodScore = scoreMood(dailyLog, reasons);
        BigDecimal distractionScore = scoreDistraction(dailyLog, reasons);
        BigDecimal reflectionScore = scoreReflection(dailyLog, reasons);

        BigDecimal dailyScore = growthScore.multiply(GROWTH_WEIGHT)
                .add(executionScore.multiply(EXECUTION_WEIGHT))
                .add(energyScore.multiply(ENERGY_WEIGHT))
                .add(moodScore.multiply(MOOD_WEIGHT))
                .add(distractionScore.multiply(DISTRACTION_WEIGHT))
                .add(reflectionScore.multiply(REFLECTION_WEIGHT));

        dailyScore = normalize(dailyScore);
        DailyRating rating = ratingOf(dailyScore);
        reasons.put("dailyScore", "每日总分按权重计算：学习成长25%，任务执行25%，精力恢复15%，情绪状态15%，娱乐控制10%，复盘质量10%。");
        reasons.put("rating", "评级按总分区间计算：S=90-100，A=80-89，B=70-79，C=60-69，D=40-59，E=0-39。");

        return new ScoreResult(
                dailyScore,
                rating.name(),
                growthScore,
                executionScore,
                energyScore,
                moodScore,
                distractionScore,
                reflectionScore,
                reasons
        );
    }

    @Transactional
    public DailyScoreEntity calculateAndSave(DailyLogEntity dailyLog) {
        ScoreResult result = calculate(dailyLog);
        DailyScoreEntity score = dailyScoreRepository
                .findByUserIdAndLogDate(dailyLog.getUserId(), dailyLog.getLogDate())
                .orElseGet(DailyScoreEntity::new);

        score.setUserId(dailyLog.getUserId());
        score.setDailyLogId(dailyLog.getId());
        score.setLogDate(dailyLog.getLogDate());
        score.setDailyScore(result.dailyScore());
        score.setGrowthScore(result.growthScore());
        score.setExecutionScore(result.executionScore());
        score.setEnergyScore(result.energyScore());
        score.setMoodScore(result.moodScore());
        score.setDistractionScore(result.distractionScore());
        score.setReflectionScore(result.reflectionScore());
        score.setRating(DailyRating.valueOf(result.rating()));
        score.setReasonJson(toJson(result.reasons()));

        return dailyScoreRepository.save(score);
    }

    private BigDecimal scoreGrowth(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        double studyHours = hours(dailyLog.getStudyHours());
        double workHours = hours(dailyLog.getWorkHours());
        double score = Math.min(studyHours / 4.0 * 80.0, 80.0)
                + Math.min(workHours / 2.0 * 15.0, 15.0)
                + (hasText(dailyLog.getCompletedContent()) ? 5.0 : 0.0);
        BigDecimal result = score(score);
        reasons.put("growthScore", String.format(
                "学习成长由学习时长、工作/项目推进和完成内容计算：学习 %.1f 小时，工作 %.1f 小时，完成内容%s。",
                studyHours,
                workHours,
                hasText(dailyLog.getCompletedContent()) ? "已填写" : "未填写"
        ));
        return result;
    }

    private BigDecimal scoreExecution(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        Integer taskCompletionRate = dailyLog.getTaskCompletionRate();
        double score = taskCompletionRate == null
                ? (hasText(dailyLog.getCompletedContent()) ? 50.0 : 40.0)
                : taskCompletionRate;
        reasons.put("executionScore", taskCompletionRate == null
                ? "任务完成率缺失，按完成内容是否填写给出保守执行分。"
                : "任务执行得分直接来自后端校验后的任务完成率。");
        return score(score);
    }

    private BigDecimal scoreEnergy(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        double sleepHours = hours(dailyLog.getSleepHours());
        int exerciseMinutes = minutes(dailyLog.getExerciseMinutes());
        double sleepScore = sleepScore(sleepHours);
        double exerciseBonus = Math.min(exerciseMinutes / 30.0 * 10.0, 20.0);
        BigDecimal result = score(sleepScore + exerciseBonus);
        reasons.put("energyScore", String.format(
                "精力恢复由睡眠和运动计算：睡眠 %.1f 小时得到基础分，运动 %d 分钟带来恢复加成。",
                sleepHours,
                exerciseMinutes
        ));
        return result;
    }

    private BigDecimal scoreMood(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        String moodTag = dailyLog.getMoodTag();
        double score = moodScore(moodTag);
        reasons.put("moodScore", hasText(moodTag)
                ? "情绪状态根据 moodTag 的积极、平稳、压力或低落程度映射为规则分。"
                : "情绪标签缺失，情绪状态采用中性偏保守分。");
        return score(score);
    }

    private BigDecimal scoreDistraction(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        int entertainmentMinutes = minutes(dailyLog.getEntertainmentMinutes());
        double score;
        if (entertainmentMinutes <= 30) {
            score = 100.0;
        } else if (entertainmentMinutes <= 60) {
            score = 85.0;
        } else if (entertainmentMinutes <= 120) {
            score = 70.0;
        } else if (entertainmentMinutes <= 180) {
            score = 50.0;
        } else {
            score = 30.0;
        }
        if (containsAny(dailyLog.getProblemText(), "分心", "刷视频", "拖延")
                || containsAny(dailyLog.getRawText(), "分心", "刷视频", "拖延")) {
            score -= 10.0;
        }
        reasons.put("distractionScore", String.format(
                "娱乐控制由娱乐时长和分心风险计算：娱乐 %d 分钟，日志中的分心/拖延风险会扣分。",
                entertainmentMinutes
        ));
        return score(score);
    }

    private BigDecimal scoreReflection(DailyLogEntity dailyLog, LinkedHashMap<String, String> reasons) {
        int reflectionLength = textLength(dailyLog.getReflectionText());
        double score;
        if (reflectionLength >= 80) {
            score = 100.0;
        } else if (reflectionLength >= 40) {
            score = 85.0;
        } else if (reflectionLength >= 15) {
            score = 70.0;
        } else if (hasText(dailyLog.getProblemText()) || hasText(dailyLog.getCompletedContent())) {
            score = 55.0;
        } else {
            score = 40.0;
        }
        reasons.put("reflectionScore", String.format(
                "复盘质量由复盘文本长度和是否记录问题/完成内容计算：复盘文本 %d 个字符。",
                reflectionLength
        ));
        return score(score);
    }

    private double sleepScore(double sleepHours) {
        if (sleepHours <= 0) {
            return 35.0;
        }
        if (sleepHours < 4.0) {
            return 35.0;
        }
        if (sleepHours < 6.0) {
            return 55.0;
        }
        if (sleepHours < 8.0) {
            return 80.0;
        }
        if (sleepHours <= 9.0) {
            return 100.0;
        }
        return 75.0;
    }

    private double moodScore(String moodTag) {
        if (!hasText(moodTag)) {
            return 60.0;
        }
        if (containsAny(moodTag, "开心", "高兴", "愉快", "兴奋", "满足")) {
            return 90.0;
        }
        if (containsAny(moodTag, "平静", "稳定", "普通", "还行")) {
            return 75.0;
        }
        if (containsAny(moodTag, "焦虑", "紧张", "烦躁", "压力")) {
            return 55.0;
        }
        if (containsAny(moodTag, "疲惫", "低落", "难过", "沮丧")) {
            return 45.0;
        }
        return 65.0;
    }

    private BigDecimal score(double value) {
        return normalize(BigDecimal.valueOf(Math.max(0.0, Math.min(100.0, value))));
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private DailyRating ratingOf(BigDecimal dailyScore) {
        if (dailyScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return DailyRating.S;
        }
        if (dailyScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return DailyRating.A;
        }
        if (dailyScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return DailyRating.B;
        }
        if (dailyScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return DailyRating.C;
        }
        if (dailyScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return DailyRating.D;
        }
        return DailyRating.E;
    }

    private double hours(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private int minutes(Integer value) {
        return value == null ? 0 : value;
    }

    private int textLength(String value) {
        return hasText(value) ? value.trim().length() : 0;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private boolean containsAny(String value, String... keywords) {
        if (!hasText(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "评分原因序列化失败");
        }
    }
}
