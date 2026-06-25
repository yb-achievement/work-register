package com.example.workregister.dto;

/**
 * 工作列表查询条件。
 */
public class WorkItemQueryRequest {

    /**
     * 按工作主题精确筛选。
     */
    private String topic;

    /**
     * 按执行人模糊筛选。
     */
    private String executor;

    /**
     * 展示状态筛选：overdue、pending、done。
     */
    private String displayStatus;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }
}
