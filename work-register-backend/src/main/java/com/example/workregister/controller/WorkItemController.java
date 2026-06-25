package com.example.workregister.controller;

import com.example.workregister.common.Result;
import com.example.workregister.dto.WorkItemCreateRequest;
import com.example.workregister.dto.WorkItemQueryRequest;
import com.example.workregister.dto.WorkItemUpdateRequest;
import com.example.workregister.service.WorkItemService;
import com.example.workregister.vo.SummaryVO;
import com.example.workregister.vo.WeeklyContentSummaryVO;
import com.example.workregister.vo.WorkItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作记录 REST 接口。
 */
@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    /**
     * 新增工作记录，默认状态为未完成。
     */
    @PostMapping
    public Result<WorkItemVO> create(@Valid @RequestBody WorkItemCreateRequest request) {
        return Result.success(workItemService.create(request));
    }

    /**
     * 查询工作列表，支持按主题、执行人和展示状态筛选。
     */
    @GetMapping
    public Result<List<WorkItemVO>> list(@ModelAttribute WorkItemQueryRequest request) {
        return Result.success(workItemService.list(request));
    }

    /**
     * 查询单条工作记录详情。
     */
    @GetMapping("/{id}")
    public Result<WorkItemVO> detail(@PathVariable Long id) {
        return Result.success(workItemService.getById(id));
    }

    /**
     * 修改工作记录的基础信息。
     */
    @PutMapping("/{id}")
    public Result<WorkItemVO> update(@PathVariable Long id, @Valid @RequestBody WorkItemUpdateRequest request) {
        return Result.success(workItemService.update(id, request));
    }

    /**
     * 软删除工作记录。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workItemService.delete(id);
        return Result.success();
    }

    /**
     * 将工作记录确认完成。
     */
    @PatchMapping("/{id}/complete")
    public Result<WorkItemVO> complete(@PathVariable Long id) {
        return Result.success(workItemService.complete(id));
    }

    /**
     * 获取周、月或年度统计。
     */
    @GetMapping("/summary")
    public Result<SummaryVO> summary(@RequestParam String type) {
        return Result.success(workItemService.summary(type));
    }

    /**
     * 生成规则版周工作内容总结。
     */
    @GetMapping("/weekly-content-summary")
    public Result<WeeklyContentSummaryVO> weeklyContentSummary() {
        return Result.success(workItemService.weeklyContentSummary());
    }
}
