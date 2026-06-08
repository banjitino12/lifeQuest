package com.lifequest.llm.entity;

import com.lifequest.common.entity.BaseEntity;
import com.lifequest.common.enums.LlmFeedbackStatus;
import com.lifequest.common.enums.LlmFeedbackType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "llm_feedback")
public class LlmFeedbackEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "daily_log_id")
    private Long dailyLogId;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 64)
    private LlmFeedbackType feedbackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private LlmFeedbackStatus status = LlmFeedbackStatus.PENDING;

    @Lob
    @Column(name = "prompt")
    private String prompt;

    @Lob
    @Column(name = "response")
    private String response;

    @Lob
    @Column(name = "fallback_response")
    private String fallbackResponse;

    @Column(name = "model_name", length = 128)
    private String modelName;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;
}
