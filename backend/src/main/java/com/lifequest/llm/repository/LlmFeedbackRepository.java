package com.lifequest.llm.repository;

import com.lifequest.common.enums.LlmFeedbackStatus;
import com.lifequest.common.enums.LlmFeedbackType;
import com.lifequest.llm.entity.LlmFeedbackEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmFeedbackRepository extends JpaRepository<LlmFeedbackEntity, Long> {

    List<LlmFeedbackEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<LlmFeedbackEntity> findByUserIdAndDailyLogId(Long userId, Long dailyLogId);

    List<LlmFeedbackEntity> findByFeedbackTypeAndStatus(
            LlmFeedbackType feedbackType,
            LlmFeedbackStatus status
    );
}
