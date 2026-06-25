package com.example.workregister.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workregister.dto.WorkItemCreateRequest;
import com.example.workregister.dto.WorkItemQueryRequest;
import com.example.workregister.dto.WorkItemUpdateRequest;
import com.example.workregister.entity.WorkItem;
import com.example.workregister.exception.BusinessException;
import com.example.workregister.mapper.WorkItemMapper;
import com.example.workregister.service.WorkItemService;
import com.example.workregister.vo.SummaryVO;
import com.example.workregister.vo.WeeklyContentSummaryVO;
import com.example.workregister.vo.WorkItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

/**
 * 工作记录业务实现。
 */
@Service
public class WorkItemServiceImpl implements WorkItemService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_DONE = "done";

    private final WorkItemMapper workItemMapper;

    public WorkItemServiceImpl(WorkItemMapper workItemMapper) {
        this.workItemMapper = workItemMapper;
    }

    @Override
    @Transactional
    public WorkItemVO create(WorkItemCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 新增时只写入真实状态 pending，超时展示状态由 dueDate 动态计算。
        WorkItem workItem = new WorkItem();
        workItem.setTopic(request.getTopic());
        workItem.setContent(request.getContent());
        workItem.setExecutor(request.getExecutor());
        workItem.setDueDate(request.getDueDate());
        workItem.setStatus(STATUS_PENDING);
        workItem.setCreatedAt(now);
        workItem.setUpdatedAt(now);

        workItemMapper.insert(workItem);
        return toVO(workItem);
    }

    @Override
    public List<WorkItemVO> list(WorkItemQueryRequest request) {
        List<WorkItem> items = workItemMapper.selectList(baseQuery(request));

        return items.stream()
                .filter(item -> matchDisplayStatus(item, request.getDisplayStatus()))
                // 首页展示顺序固定为：超时未完成、未完成、已完成。
                .sorted(Comparator
                        .comparingInt(this::displayOrder)
                        .thenComparing(WorkItem::getDueDate)
                        .thenComparing(WorkItem::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toVO)
                .toList();
    }

    @Override
    public WorkItemVO getById(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    @Transactional
    public WorkItemVO update(Long id, WorkItemUpdateRequest request) {
        WorkItem workItem = getExisting(id);
        workItem.setTopic(request.getTopic());
        workItem.setContent(request.getContent());
        workItem.setExecutor(request.getExecutor());
        workItem.setDueDate(request.getDueDate());
        workItem.setUpdatedAt(LocalDateTime.now());

        workItemMapper.updateById(workItem);
        return toVO(workItem);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WorkItem workItem = getExisting(id);
        LocalDateTime now = LocalDateTime.now();

        // 第一版采用软删除，保留历史数据便于后续恢复或审计。
        workItem.setDeletedAt(now);
        workItem.setUpdatedAt(now);
        workItemMapper.updateById(workItem);
    }

    @Override
    @Transactional
    public WorkItemVO complete(Long id) {
        WorkItem workItem = getExisting(id);
        if (STATUS_DONE.equals(workItem.getStatus())) {
            return toVO(workItem);
        }

        LocalDateTime now = LocalDateTime.now();
        workItem.setStatus(STATUS_DONE);
        workItem.setCompletedAt(now);
        workItem.setUpdatedAt(now);
        workItemMapper.updateById(workItem);
        return toVO(workItem);
    }

    @Override
    public SummaryVO summary(String type) {
        PeriodRange range = getPeriodRange(type);

        // 统计使用左闭右开区间，避免漏掉结束日期当天的记录。
        List<WorkItem> items = workItemMapper.selectList(new LambdaQueryWrapper<WorkItem>()
                .isNull(WorkItem::getDeletedAt)
                .ge(WorkItem::getCreatedAt, range.start().atStartOfDay())
                .lt(WorkItem::getCreatedAt, range.end().plusDays(1).atStartOfDay()));

        SummaryVO vo = new SummaryVO();
        vo.setSummaryType(type);
        vo.setPeriodStart(range.start());
        vo.setPeriodEnd(range.end());
        vo.setTotalCount(items.size());
        vo.setDoneCount(items.stream().filter(item -> STATUS_DONE.equals(item.getStatus())).count());
        vo.setPendingCount(items.stream().filter(item -> "pending".equals(getDisplayStatus(item))).count());
        vo.setOverdueCount(items.stream().filter(item -> "overdue".equals(getDisplayStatus(item))).count());
        return vo;
    }

    @Override
    public WeeklyContentSummaryVO weeklyContentSummary() {
        PeriodRange range = getPeriodRange("week");

        // “本周已完成工作”只取当前周范围内登记且已完成的记录。
        List<WorkItem> completedThisWeek = workItemMapper.selectList(new LambdaQueryWrapper<WorkItem>()
                .isNull(WorkItem::getDeletedAt)
                .eq(WorkItem::getStatus, STATUS_DONE)
                .ge(WorkItem::getCreatedAt, range.start().atStartOfDay())
                .lt(WorkItem::getCreatedAt, range.end().plusDays(1).atStartOfDay())
                .orderByAsc(WorkItem::getCreatedAt));

        // “下周工作”来自当前所有未完成记录，包括已超时和未超时。
        List<WorkItem> unfinished = workItemMapper.selectList(new LambdaQueryWrapper<WorkItem>()
                .isNull(WorkItem::getDeletedAt)
                .eq(WorkItem::getStatus, STATUS_PENDING)
                .orderByAsc(WorkItem::getDueDate)
                .orderByDesc(WorkItem::getCreatedAt));

        WeeklyContentSummaryVO vo = new WeeklyContentSummaryVO();
        vo.setCompletedThisWeek(completedThisWeek.stream().map(this::formatSummaryLine).toList());
        vo.setNextWeekWork(unfinished.stream().map(this::formatSummaryLine).toList());
        vo.setModelType("rule");
        return vo;
    }

    private LambdaQueryWrapper<WorkItem> baseQuery(WorkItemQueryRequest request) {
        LambdaQueryWrapper<WorkItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(WorkItem::getDeletedAt);

        if (StringUtils.hasText(request.getTopic())) {
            wrapper.eq(WorkItem::getTopic, request.getTopic());
        }

        if (StringUtils.hasText(request.getExecutor())) {
            wrapper.like(WorkItem::getExecutor, request.getExecutor());
        }

        return wrapper;
    }

    /**
     * 获取存在且未软删除的工作记录。
     */
    private WorkItem getExisting(Long id) {
        WorkItem workItem = workItemMapper.selectById(id);
        if (workItem == null || workItem.getDeletedAt() != null) {
            throw new BusinessException("工作记录不存在");
        }
        return workItem;
    }

    /**
     * 判断记录是否匹配页面展示状态筛选。
     */
    private boolean matchDisplayStatus(WorkItem item, String displayStatus) {
        return !StringUtils.hasText(displayStatus) || displayStatus.equals(getDisplayStatus(item));
    }

    /**
     * 计算首页分组排序权重。
     */
    private int displayOrder(WorkItem item) {
        return switch (getDisplayStatus(item)) {
            case "overdue" -> 1;
            case "pending" -> 2;
            case "done" -> 3;
            default -> 4;
        };
    }

    /**
     * 根据真实状态和要求完成日期计算页面展示状态。
     */
    private String getDisplayStatus(WorkItem item) {
        if (STATUS_DONE.equals(item.getStatus())) {
            return "done";
        }

        if (item.getDueDate().isBefore(LocalDate.now())) {
            return "overdue";
        }

        return "pending";
    }

    /**
     * 将数据库实体转换为接口返回对象。
     */
    private WorkItemVO toVO(WorkItem item) {
        WorkItemVO vo = new WorkItemVO();
        vo.setId(item.getId());
        vo.setTopic(item.getTopic());
        vo.setContent(item.getContent());
        vo.setExecutor(item.getExecutor());
        vo.setDueDate(item.getDueDate());
        vo.setStatus(item.getStatus());
        vo.setDisplayStatus(getDisplayStatus(item));
        vo.setCreatedAt(item.getCreatedAt());
        vo.setCompletedAt(item.getCompletedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }

    /**
     * 根据总结类型计算统计日期范围。
     */
    private PeriodRange getPeriodRange(String type) {
        LocalDate today = LocalDate.now();
        return switch (type) {
            case "week" -> getWeekRange(today);
            case "month" -> new PeriodRange(today.withDayOfMonth(1), today.with(TemporalAdjusters.lastDayOfMonth()));
            case "year" -> new PeriodRange(today.withDayOfYear(1), today.with(TemporalAdjusters.lastDayOfYear()));
            default -> throw new BusinessException("总结类型只能是 week、month、year");
        };
    }

    /**
     * 周范围固定为上星期五至本星期四。
     */
    private PeriodRange getWeekRange(LocalDate today) {
        int diffToFriday = today.getDayOfWeek().getValue() >= DayOfWeek.FRIDAY.getValue()
                ? today.getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue()
                : today.getDayOfWeek().getValue() + 2;
        LocalDate start = today.minusDays(diffToFriday);
        return new PeriodRange(start, start.plusDays(6));
    }

    /**
     * 周总结中每条工作使用“主题：内容”的格式。
     */
    private String formatSummaryLine(WorkItem item) {
        return item.getTopic() + "：" + item.getContent();
    }

    /**
     * 统计日期范围。
     */
    private record PeriodRange(LocalDate start, LocalDate end) {
    }
}
