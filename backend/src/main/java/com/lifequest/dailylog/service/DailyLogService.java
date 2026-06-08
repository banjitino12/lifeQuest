package com.lifequest.dailylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.enums.DailyLogSourceType;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.dailylog.dto.DailyLogSummaryResponse;
import com.lifequest.dailylog.dto.DailyLogSubmitResult;
import com.lifequest.dailylog.dto.SubmitDailyLogRequest;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.llm.model.LlmParseResult;
import com.lifequest.llm.service.LlmService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DailyLogService {

    private static final double MAX_HOURS_PER_DAY = 24.0;
    private static final int MAX_MINUTES_PER_DAY = 24 * 60;

    private final CurrentUserService currentUserService;
    private final DailyLogRepository dailyLogRepository;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public DailyLogService(
            CurrentUserService currentUserService,
            DailyLogRepository dailyLogRepository,
            LlmService llmService,
            ObjectMapper objectMapper
    ) {
        this.currentUserService = currentUserService;
        this.dailyLogRepository = dailyLogRepository;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DailyLogSummaryResponse submit(SubmitDailyLogRequest request) {
        DailyLogSubmitResult result = submitAndReturnLog(request);
        DailyLogEntity savedLog = result.dailyLog();
        return new DailyLogSummaryResponse(
                savedLog.getId(),
                savedLog.getLogDate(),
                result.sourceType(),
                result.llmStatus(),
                result.updated()
        );
    }

    @Transactional
    public DailyLogSubmitResult submitAndReturnLog(SubmitDailyLogRequest request) {
        validate(request);

        Long userId = currentUserService.requireCurrentUserId();
        DailyLogSourceType sourceType = resolveSourceType(request);
        boolean updated = dailyLogRepository.existsByUserIdAndLogDate(userId, request.logDate());
        DailyLogEntity dailyLog = dailyLogRepository.findByUserIdAndLogDate(userId, request.logDate())
                .orElseGet(DailyLogEntity::new);

        LlmParseResult parseResult = parseRawTextIfNeeded(request, sourceType);

        dailyLog.setUserId(userId);
        dailyLog.setLogDate(request.logDate());
        dailyLog.setRawText(normalizeOptional(request.rawText()));
        dailyLog.setStudyHours(toBigDecimal(request.studyHours()));
        dailyLog.setWorkHours(toBigDecimal(request.workHours()));
        dailyLog.setSleepHours(toBigDecimal(request.sleepHours()));
        dailyLog.setExerciseMinutes(firstPresent(request.exerciseMinutes(), parseResult.exerciseMinutes(), 0));
        dailyLog.setEntertainmentMinutes(firstPresent(request.entertainmentMinutes(), parseResult.entertainmentMinutes(), 0));
        dailyLog.setMoodTag(firstText(request.moodTag(), parseResult.mood()));
        dailyLog.setTaskCompletionRate(request.taskCompletionRate());
        dailyLog.setCompletedContent(normalizeOptional(request.completedContent()));
        dailyLog.setProblemText(normalizeOptional(request.problemText()));
        dailyLog.setReflectionText(normalizeOptional(request.reflectionText()));
        dailyLog.setParsedJson(toJson(parseResult));
        dailyLog.setSourceType(sourceType);

        DailyLogEntity savedLog = dailyLogRepository.save(dailyLog);
        return new DailyLogSubmitResult(savedLog, savedLog.getSourceType().name(), parseResult.status(), updated);
    }

    private void validate(SubmitDailyLogRequest request) {
        assertHourRange("studyHours", request.studyHours());
        assertHourRange("workHours", request.workHours());
        assertHourRange("sleepHours", request.sleepHours());
        assertMinuteRange("exerciseMinutes", request.exerciseMinutes());
        assertMinuteRange("entertainmentMinutes", request.entertainmentMinutes());
        assertPercentageRange("taskCompletionRate", request.taskCompletionRate());
        if (request.sourceType() != null) {
            parseSourceType(request.sourceType());
        }
    }

    private DailyLogSourceType resolveSourceType(SubmitDailyLogRequest request) {
        if (StringUtils.hasText(request.sourceType())) {
            return parseSourceType(request.sourceType());
        }
        return StringUtils.hasText(request.rawText()) ? DailyLogSourceType.NATURAL_LANGUAGE : DailyLogSourceType.FORM;
    }

    private DailyLogSourceType parseSourceType(String sourceType) {
        try {
            return DailyLogSourceType.valueOf(sourceType.trim());
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "sourceType 不支持该取值");
        }
    }

    private LlmParseResult parseRawTextIfNeeded(SubmitDailyLogRequest request, DailyLogSourceType sourceType) {
        if (!StringUtils.hasText(request.rawText()) || sourceType == DailyLogSourceType.FORM) {
            return fallbackParseResult("SKIPPED");
        }
        try {
            LlmParseResult result = llmService.parseDailyLog(request.rawText());
            return result == null ? fallbackParseResult("FALLBACK") : result;
        } catch (RuntimeException exception) {
            return fallbackParseResult("FAILED");
        }
    }

    private LlmParseResult fallbackParseResult(String status) {
        return new LlmParseResult(status, List.of(), List.of(), null, null, null, List.of());
    }

    private void assertHourRange(String field, Double value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > MAX_HOURS_PER_DAY) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, field + " 必须在 0 到 24 之间");
        }
    }

    private void assertMinuteRange(String field, Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > MAX_MINUTES_PER_DAY) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, field + " 必须在 0 到 1440 之间");
        }
    }

    private void assertPercentageRange(String field, Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, field + " 必须在 0 到 100 之间");
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private Integer firstPresent(Integer primary, Integer secondary, Integer fallback) {
        if (primary != null) {
            return primary;
        }
        if (secondary != null) {
            return secondary;
        }
        return fallback;
    }

    private String firstText(String primary, String secondary) {
        String normalizedPrimary = normalizeOptional(primary);
        if (normalizedPrimary != null) {
            return normalizedPrimary;
        }
        return normalizeOptional(secondary);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String toJson(LlmParseResult parseResult) {
        try {
            return objectMapper.writeValueAsString(parseResult);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "解析结果序列化失败");
        }
    }
}
