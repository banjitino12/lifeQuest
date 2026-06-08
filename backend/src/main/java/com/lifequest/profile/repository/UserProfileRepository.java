package com.lifequest.profile.repository;

import com.lifequest.profile.entity.UserProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

    Optional<UserProfileEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
