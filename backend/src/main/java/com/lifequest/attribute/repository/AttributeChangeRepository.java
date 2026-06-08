package com.lifequest.attribute.repository;

import com.lifequest.attribute.entity.AttributeChangeEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeChangeRepository extends JpaRepository<AttributeChangeEntity, Long> {

    Optional<AttributeChangeEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    Optional<AttributeChangeEntity> findByUserIdAndDailyLogId(Long userId, Long dailyLogId);

    List<AttributeChangeEntity> findByUserIdAndLogDateBetweenOrderByLogDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
