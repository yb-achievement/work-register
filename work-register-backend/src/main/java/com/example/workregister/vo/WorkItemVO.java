package com.example.workregister.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作记录返回对象。
 */
public class WorkItemVO {

    /**
     * 工作记录主键。
     */
    private Long id;

    /**
     * 工作主题。
     */
    private String topic;

    /**
     * 工作内容。
     */
    private String content;

    /**
     * 执行人。
     */
    private String executor;

    /**
     * 要求完成日期。
     */
    private LocalDate dueDate;

    /**
     * 数据库存储状态：pending 或 done。
     */
    private String status;

    /**
     * 页面展示状态：overdue、pending、done。
     */
    private String displayStatus;

    /**
     * 登记时间。
     */
    private LocalDateTime createdAt;

    /**
     * 确认完成时间。
     */
    private LocalDateTime completedAt;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
