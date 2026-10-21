package com.campus.system.log.service;

import com.campus.common.excel.ExcelService;
import com.campus.common.page.PageResult;
import com.campus.system.log.dto.OperLogExportRow;
import com.campus.system.log.dto.OperLogQuery;
import com.campus.system.log.entity.OperationLog;
import com.campus.system.log.mapper.OperLogMapper;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperLogService {

    private final OperLogMapper operLogMapper;
    private final ExcelService excelService;

    @Async("logExecutor")
    public void recordAsync(OperationLog operLog) {
        operLogMapper.insert(operLog);
    }

    public PageResult<OperationLog> page(OperLogQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(), orderBy != null ? orderBy : "id DESC");
        return PageResult.of(operLogMapper.selectPage(query));
    }

    public OperationLog getById(Long id) {
        return operLogMapper.selectById(id);
    }

    public void delete(Long id) {
        operLogMapper.deleteById(id);
    }

    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            operLogMapper.deleteByIds(ids);
        }
    }

    public void clean(LocalDateTime before) {
        operLogMapper.deleteBefore(before);
    }

    public List<OperationLog> listForExport(OperLogQuery query) {
        return operLogMapper.selectForExport(query);
    }

    public void export(HttpServletResponse response, OperLogQuery query) {
        List<OperLogExportRow> rows = operLogMapper.selectForExport(query).stream()
                .map(this::toExportRow)
                .toList();
        excelService.write(response, "操作日志.xlsx", "操作日志", OperLogExportRow.class, rows);
    }

    private OperLogExportRow toExportRow(OperationLog log) {
        OperLogExportRow row = new OperLogExportRow();
        row.setTitle(log.getTitle());
        row.setBusinessType(log.getBusinessType());
        row.setOperName(log.getOperName());
        row.setOperIp(log.getOperIp());
        row.setOperUrl(log.getOperUrl());
        row.setStatus(log.getSuccess() != null && log.getSuccess() == 1 ? "成功" : "失败");
        row.setCostTime(log.getCostTime());
        row.setOperTime(log.getOperTime());
        row.setOperParam(log.getOperParam());
        return row;
    }
}
