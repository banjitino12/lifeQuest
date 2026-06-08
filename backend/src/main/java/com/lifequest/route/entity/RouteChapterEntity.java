package com.lifequest.route.entity;

import com.lifequest.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_chapter")
public class RouteChapterEntity extends BaseEntity {

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "chapter_no", nullable = false)
    private Integer chapterNo;

    @Column(name = "chapter_name", nullable = false, length = 128)
    private String chapterName;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "target_keywords", length = 512)
    private String targetKeywords;
}
