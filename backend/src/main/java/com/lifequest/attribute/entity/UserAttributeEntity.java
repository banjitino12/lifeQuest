package com.lifequest.attribute.entity;

import com.lifequest.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_attribute")
public class UserAttributeEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "focus", nullable = false)
    private Integer focus = 50;

    @Column(name = "discipline", nullable = false)
    private Integer discipline = 50;

    @Column(name = "knowledge", nullable = false)
    private Integer knowledge = 50;

    @Column(name = "energy", nullable = false)
    private Integer energy = 50;

    @Column(name = "mood", nullable = false)
    private Integer mood = 50;

    @Column(name = "execution", nullable = false)
    private Integer execution = 50;

    @Column(name = "balance", nullable = false)
    private Integer balance = 50;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "exp", nullable = false)
    private Integer exp = 0;

    @Column(name = "total_exp", nullable = false)
    private Integer totalExp = 0;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getFocus() {
        return focus;
    }

    public void setFocus(Integer focus) {
        this.focus = focus;
    }

    public Integer getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Integer discipline) {
        this.discipline = discipline;
    }

    public Integer getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Integer knowledge) {
        this.knowledge = knowledge;
    }

    public Integer getEnergy() {
        return energy;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public Integer getExecution() {
        return execution;
    }

    public void setExecution(Integer execution) {
        this.execution = execution;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public Integer getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(Integer totalExp) {
        this.totalExp = totalExp;
    }
}
