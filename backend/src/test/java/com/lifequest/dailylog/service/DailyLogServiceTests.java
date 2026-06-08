package com.lifequest.dailylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.enums.DailyLogSourceType;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.dailylog.dto.DailyLogSummaryResponse;
import com.lifequest.dailylog.dto.SubmitDailyLogRequest;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.llm.model.LlmParseResult;
import com.lifequest.llm.service.LlmService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DailyLogServiceTests {

    private final CurrentUserService currentUserService = org.mockito.Mockito.mock(CurrentUserService.class);
    private final DailyLogRepository dailyLogRepository = org.mockito.Mockito.mock(DailyLogRepository.class);
    private final LlmService llmService = org.mockito.Mockito.mock(LlmService.class);
    private final DailyLogService dailyLogService = new DailyLogService(
            currentUserService,
            dailyLogRepository,
            llmService,
            new ObjectMapper()
    );

    @Test
    void submitCreatesDailyLogForCurrentUserAndUsesLlmFieldsAsFallback() {
        LocalDate logDate = LocalDate.of(2026, 6, 9);
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(dailyLogRepository.existsByUserIdAndLogDate(7L, logDate)).thenReturn(false);
        when(dailyLogRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.empty());
        when(llmService.parseDailyLog("今天学习 Redis，运动 20 分钟")).thenReturn(new LlmParseResult(
                "SUCCESS",
                List.of("Redis"),
                List.of(),
                "平静",
                30,
                20,
                List.of()
        ));
        when(dailyLogRepository.save(any(DailyLogEntity.class))).thenAnswer(invocation -> {
            DailyLogEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return entity;
        });

        DailyLogSummaryResponse response = dailyLogService.submit(new SubmitDailyLogRequest(
                logDate,
                "今天学习 Redis，运动 20 分钟",
                3.0,
                null,
                7.0,
                null,
                null,
                null,
                80,
                "复习 Redis",
                null,
                "效率不错",
                "MIXED"
        ));

        assertThat(response.dailyLogId()).isEqualTo(101L);
        assertThat(response.logDate()).isEqualTo(logDate);
        assertThat(response.sourceType()).isEqualTo("MIXED");
        assertThat(response.llmStatus()).isEqualTo("SUCCESS");
        assertThat(response.updated()).isFalse();
        verify(dailyLogRepository).save(any(DailyLogEntity.class));
    }

    @Test
    void submitUpdatesExistingDailyLogForSameUserAndDate() {
        LocalDate logDate = LocalDate.of(2026, 6, 9);
        DailyLogEntity existing = new DailyLogEntity();
        existing.setId(88L);
        existing.setUserId(7L);
        existing.setLogDate(logDate);
        existing.setStudyHours(BigDecimal.ONE);
        existing.setSourceType(DailyLogSourceType.FORM);
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(dailyLogRepository.existsByUserIdAndLogDate(7L, logDate)).thenReturn(true);
        when(dailyLogRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.of(existing));
        when(dailyLogRepository.save(any(DailyLogEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyLogSummaryResponse response = dailyLogService.submit(new SubmitDailyLogRequest(
                logDate,
                null,
                4.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "FORM"
        ));

        assertThat(response.dailyLogId()).isEqualTo(88L);
        assertThat(response.updated()).isTrue();
        assertThat(existing.getStudyHours()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
    }

    @Test
    void submitRejectsOutOfRangeFields() {
        assertThatThrownBy(() -> dailyLogService.submit(new SubmitDailyLogRequest(
                LocalDate.of(2026, 6, 9),
                null,
                25.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "FORM"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        assertThatThrownBy(() -> dailyLogService.submit(new SubmitDailyLogRequest(
                LocalDate.of(2026, 6, 9),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                101,
                null,
                null,
                null,
                "FORM"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void submitKeepsRawTextAndFormFieldsWhenLlmFails() {
        LocalDate logDate = LocalDate.of(2026, 6, 9);
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(dailyLogRepository.findByUserIdAndLogDate(7L, logDate)).thenReturn(Optional.empty());
        when(llmService.parseDailyLog("今天有点焦虑")).thenThrow(new IllegalStateException("llm unavailable"));
        when(dailyLogRepository.save(any(DailyLogEntity.class))).thenAnswer(invocation -> {
            DailyLogEntity entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        DailyLogSummaryResponse response = dailyLogService.submit(new SubmitDailyLogRequest(
                logDate,
                "今天有点焦虑",
                2.0,
                null,
                6.0,
                null,
                90,
                "焦虑",
                60,
                "完成接口",
                "分心",
                "需要早点睡",
                "MIXED"
        ));

        assertThat(response.llmStatus()).isEqualTo("FAILED");
        verify(dailyLogRepository).save(org.mockito.ArgumentMatchers.argThat(entity ->
                "今天有点焦虑".equals(entity.getRawText())
                        && BigDecimal.valueOf(2.0).compareTo(entity.getStudyHours()) == 0
                        && entity.getEntertainmentMinutes().equals(90)
                        && "焦虑".equals(entity.getMoodTag())
                        && entity.getParsedJson().contains("\"status\":\"FAILED\"")
        ));
    }
}
