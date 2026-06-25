package com.example.workregister.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作记录实体，对应数据库 work_items 表。
 */
@TableName("work_items")
public class WorkItem {

    /**
     * 工作记录主键，自增生成。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工作主题：数据、系统功能、常规性工作。
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
     * 真实存储状态：pending 或 done。超时状态由 dueDate 动态计算。
     */
    private String status;

    /**
     * 登记时间。
     */
    private LocalDateTime createdAt;

    /**
     * 确认完成时间，未完成时为空。
     */
    private LocalDateTime completedAt;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除时间，空表示未删除。
     */
    private LocalDateTime deletedAt;

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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
