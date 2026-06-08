package com.lifequest.route.entity;

import com.lifequest.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_level")
public class RouteLevelEntity extends BaseEntity {

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo;

    @Column(name = "level_name", nullable = false, length = 128)
    private String levelName;

    @Column(name = "description", length = 512)
    private String description;

    @Lob
    @Column(name = "completion_rule_json", columnDefinition = "json")
    private String completionRuleJson;

    @Column(name = "exp_reward", nullable = false)
    private Integer expReward = 0;
}
