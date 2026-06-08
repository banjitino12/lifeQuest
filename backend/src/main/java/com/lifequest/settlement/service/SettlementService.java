package com.lifequest.settlement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.attribute.entity.AttributeChangeEntity;
import com.lifequest.attribute.model.AttributeChangeResult;
import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.service.AttributeService;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SettlementService {

    private static final TypeReference<Map<String, String>> REASON_MAP_TYPE = new TypeReference<>() {
    };

    private final CurrentUserService currentUserService;
    private final DailyLogService dailyLogService;
    private final DailyLogRepository dailyLogRepository;
    private final ScoringService scoringService;
    private final DailyScoreRepository dailyScoreRepository;
    private final AttributeService attributeService;
    private final AttributeChangeRepository attributeChangeRepository;
    private final GamificationService gamificationService;
    private final GameEventRepository gameEventRepository;
    private final ObjectMapper objectMapper;

    public SettlementService(
            CurrentUserService currentUserService,
            DailyLogService dailyLogService,
            DailyLogRepository dailyLogRepository,
            ScoringService scoringService,
            DailyScoreRepository dailyScoreRepository,
            AttributeService attributeService,
            AttributeChangeRepository attributeChangeRepository,
            GamificationService gamificationService,
            GameEventRepository gameEventRepository,
            ObjectMapper objectMapper
    ) {
        this.currentUserService = currentUserService;
        this.dailyLogService = dailyLogService;
        this.dailyLogRepository = dailyLogRepository;
        this.scoringService = scoringService;
        this.dailyScoreRepository = dailyScoreRepository;
        this.attributeService = attributeService;
        this.attributeChangeRepository = attributeChangeRepository;
        this.gamificationService = gamificationService;
        this.gameEventRepository = gameEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SettlementResponse submitAndSettle(SubmitDailyLogRequest request) {
        DailyLogSubmitResult submitResult = dailyLogService.submitAndReturnLog(request);
        DailyLogEntity dailyLog = submitResult.dailyLog();
        ScoreResult scoreResult = scoringService.calculate(dailyLog);
        scoringService.calculateAndSave(dailyLog);
        AttributeChangeResult attributeChange = attributeService.calculate(dailyLog, scoreResult);
        attributeService.calculateAndSave(dailyLog, scoreResult);
        List<GameEventEntity> events = gamificationService.generateAndSaveEvents(dailyLog, scoreResult, attributeChange);
        return toResponse(dailyLog, submitResult.sourceType(), submitResult.updated(), scoreResult, attributeChange, events, submitResult.llmStatus());
    }

    @Transactional(readOnly = true)
    public SettlementResponse getByDate(LocalDate date) {
        Long userId = currentUserService.requireCurrentUserId();
        DailyLogEntity dailyLog = dailyLogRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到当天每日记录"));
        DailyScoreEntity score = dailyScoreRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到当天评分结果"));
        AttributeChangeEntity attributeChange = attributeChangeRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到当天属性变化"));
        List<GameEventEntity> events = gameEventRepository.findByUserIdAndLogDate(userId, date);
        return toResponse(dailyLog, dailyLog.getSourceType().name(), false, toScoreResult(score), toAttributeChangeResult(attributeChange), events, readLlmStatus(dailyLog));
    }

    private SettlementResponse toResponse(
            DailyLogEntity dailyLog,
            String sourceType,
            boolean dailyLogUpdated,
            ScoreResult scoreResult,
            AttributeChangeResult attributeChange,
            List<GameEventEntity> events,
            String llmStatus
    ) {
        return new SettlementResponse(
                dailyLog.getId(),
                dailyLog.getLogDate(),
                sourceType,
                dailyLogUpdated,
                scoreBlock(scoreResult),
                attributeBlock(attributeChange),
                events.stream().map(this::eventBlock).toList(),
                llmBlock(llmStatus),
                List.of(),
                basicSuggestion(scoreResult, attributeChange)
        );
    }

    private SettlementResponse.ScoreBlock scoreBlock(ScoreResult result) {
        return new SettlementResponse.ScoreBlock(
                result.dailyScore(),
                result.rating(),
                result.growthScore(),
                result.executionScore(),
                result.energyScore(),
                result.moodScore(),
                result.distractionScore(),
                result.reflectionScore(),
                result.reasons()
        );
    }

    private SettlementResponse.AttributeChangeBlock attributeBlock(AttributeChangeResult result) {
        return new SettlementResponse.AttributeChangeBlock(
                result.focusDelta(),
                result.disciplineDelta(),
                result.knowledgeDelta(),
                result.energyDelta(),
                result.moodDelta(),
                result.executionDelta(),
                result.balanceDelta(),
                result.expDelta(),
                result.reasons()
        );
    }

    private SettlementResponse.GameEventBlock eventBlock(GameEventEntity event) {
        return new SettlementResponse.GameEventBlock(
                event.getEventType().name(),
                event.getEventCode(),
                event.getEventName(),
                event.getEventLevel(),
                event.getEventDescription(),
                event.getEffectJson()
        );
    }

    private SettlementResponse.LlmBlock llmBlock(String llmStatus) {
        boolean fallbackUsed = !"SUCCESS".equals(llmStatus);
        return new SettlementResponse.LlmBlock(
                llmStatus,
                fallbackUsed,
                fallbackUsed ? "LLM 反馈暂不可用，已展示规则结算结果。" : "自然语言日志已完成基础解析，详细反馈将在后续 LLM 模块增强。",
                fallbackUsed ? "规则结算已完成，剧情旁白稍后可由 LLM 生成。" : "今日成长事件已整理完毕。"
        );
    }

    private String basicSuggestion(ScoreResult scoreResult, AttributeChangeResult attributeChange) {
        if (scoreResult.energyScore().intValue() < 60) {
            return "优先修复睡眠和恢复节奏，明天任务量建议降低一档。";
        }
        if (scoreResult.distractionScore().intValue() < 60) {
            return "明天先守住晚间娱乐边界，把高风险时段提前设为防御任务。";
        }
        if (attributeChange.knowledgeDelta() > 0 && attributeChange.executionDelta() > 0) {
            return "今天的学习推进和执行力都在增长，明天适合延续一个小而确定的主线任务。";
        }
        return "基础结算已完成，明天保持一个可执行的小目标即可。";
    }

    private ScoreResult toScoreResult(DailyScoreEntity score) {
        return new ScoreResult(
                score.getDailyScore(),
                score.getRating().name(),
                score.getGrowthScore(),
                score.getExecutionScore(),
                score.getEnergyScore(),
                score.getMoodScore(),
                score.getDistractionScore(),
                score.getReflectionScore(),
                readReasonMap(score.getReasonJson())
        );
    }

    private AttributeChangeResult toAttributeChangeResult(AttributeChangeEntity change) {
        return new AttributeChangeResult(
                change.getFocusDelta(),
                change.getDisciplineDelta(),
                change.getKnowledgeDelta(),
                change.getEnergyDelta(),
                change.getMoodDelta(),
                change.getExecutionDelta(),
                change.getBalanceDelta(),
                change.getExpDelta(),
                readReasonMap(change.getReasonJson())
        );
    }

    private String readLlmStatus(DailyLogEntity dailyLog) {
        if (!StringUtils.hasText(dailyLog.getParsedJson())) {
            return "SKIPPED";
        }
        try {
            JsonNode node = objectMapper.readTree(dailyLog.getParsedJson());
            JsonNode status = node.get("status");
            return status == null || !status.isTextual() ? "SKIPPED" : status.asText();
        } catch (Exception exception) {
            return "FAILED";
        }
    }

    private Map<String, String> readReasonMap(String reasonJson) {
        if (!StringUtils.hasText(reasonJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(reasonJson, REASON_MAP_TYPE);
        } catch (Exception exception) {
            return Map.of();
        }
    }
}
