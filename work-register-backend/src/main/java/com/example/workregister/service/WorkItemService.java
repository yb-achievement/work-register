package com.example.workregister.service;

import com.example.workregister.dto.WorkItemCreateRequest;
import com.example.workregister.dto.WorkItemQueryRequest;
import com.example.workregister.dto.WorkItemUpdateRequest;
import com.example.workregister.vo.SummaryVO;
import com.example.workregister.vo.WeeklyContentSummaryVO;
import com.example.workregister.vo.WorkItemVO;

import java.util.List;

/**
 * 工作记录业务接口。
 */
public interface WorkItemService {

    /**
     * 新增工作记录。
     */
    WorkItemVO create(WorkItemCreateRequest request);

    /**
     * 查询工作列表。
     */
    List<WorkItemVO> list(WorkItemQueryRequest request);

    /**
     * 获取工作记录详情。
     */
    WorkItemVO getById(Long id);

    /**
     * 修改工作记录。
     */
    WorkItemVO update(Long id, WorkItemUpdateRequest request);

    /**
     * 软删除工作记录。
     */
    void delete(Long id);

    /**
     * 确认工作完成。
     */
    WorkItemVO complete(Long id);

    /**
     * 统计指定周期内的工作数量。
     */
    SummaryVO summary(String type);

    /**
     * 生成周工作内容总结。
     */
    WeeklyContentSummaryVO weeklyContentSummary();
}
