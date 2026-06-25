package com.example.workregister.vo;

import java.util.List;

/**
 * 规则生成的周工作内容总结。
 */
public class WeeklyContentSummaryVO {

    /**
     * 本周已完成工作列表。
     */
    private List<String> completedThisWeek;

    /**
     * 当前未完成工作列表，作为下周工作来源。
     */
    private List<String> nextWeekWork;

    /**
     * 生成方式。当前第一版固定为 rule。
     */
    private String modelType;

    public List<String> getCompletedThisWeek() {
        return completedThisWeek;
    }

    public void setCompletedThisWeek(List<String> completedThisWeek) {
        this.completedThisWeek = completedThisWeek;
    }

    public List<String> getNextWeekWork() {
        return nextWeekWork;
    }

    public void setNextWeekWork(List<String> nextWeekWork) {
        this.nextWeekWork = nextWeekWork;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
