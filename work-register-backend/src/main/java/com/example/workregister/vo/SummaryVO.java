package com.example.workregister.vo;

import java.time.LocalDate;

/**
 * 周、月、年统计返回对象。
 */
public class SummaryVO {

    /**
     * 总结类型：week、month、year。
     */
    private String summaryType;

    /**
     * 统计开始日期。
     */
    private LocalDate periodStart;

    /**
     * 统计结束日期。
     */
    private LocalDate periodEnd;

    /**
     * 统计范围内登记工作总数。
     */
    private long totalCount;

    /**
     * 已完成工作数量。
     */
    private long doneCount;

    /**
     * 未完成且未超时工作数量。
     */
    private long pendingCount;

    /**
     * 未完成且已超时工作数量。
     */
    private long overdueCount;

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(long doneCount) {
        this.doneCount = doneCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }
}
