package com.lifequest.attribute.repository;

import com.lifequest.attribute.entity.UserAttributeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAttributeRepository extends JpaRepository<UserAttributeEntity, Long> {

    Optional<UserAttributeEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
