package com.lifequest.llm.repository;

import com.lifequest.llm.entity.TomorrowTaskEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TomorrowTaskRepository extends JpaRepository<TomorrowTaskEntity, Long> {

    List<TomorrowTaskEntity> findByUserIdAndTaskDate(Long userId, LocalDate taskDate);

    List<TomorrowTaskEntity> findByUserIdAndTaskDateBetweenOrderByTaskDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<TomorrowTaskEntity> findByUserIdAndSourceDailyLogId(Long userId, Long sourceDailyLogId);
}
