package com.example.workregister.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * 修改工作记录请求。
 */
public class WorkItemUpdateRequest {

    /**
     * 工作主题必须来自固定枚举，和新增规则保持一致。
     */
    @NotBlank(message = "请选择工作主题")
    @Pattern(regexp = "数据|系统功能|常规性工作", message = "主题只能是：数据、系统功能、常规性工作")
    private String topic;

    /**
     * 具体工作内容。
     */
    @NotBlank(message = "请填写工作内容")
    private String content;

    /**
     * 负责执行这项工作的人员。
     */
    @NotBlank(message = "请填写执行人")
    private String executor;

    /**
     * 要求完成日期，用于重新计算展示状态。
     */
    @NotNull(message = "请选择要求完成时间")
    private LocalDate dueDate;

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
}
