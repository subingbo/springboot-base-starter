package com.campus.system.log.service;

import com.campus.common.excel.ExcelService;
import com.campus.common.page.PageResult;
import com.campus.common.util.UserAgentUtils;
import com.campus.system.log.dto.LoginLogExportRow;
import com.campus.system.log.dto.LoginLogQuery;
import com.campus.system.log.entity.LoginLog;
import com.campus.system.log.mapper.LoginLogMapper;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;
    private final ExcelService excelService;

    @Async("logExecutor")
    public void recordAsync(String username, Long userId, String ip, String userAgent,
                            boolean success, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setUserId(userId);
        loginLog.setIp(ip);
        loginLog.setBrowser(UserAgentUtils.browser(userAgent));
        loginLog.setOs(UserAgentUtils.os(userAgent));
        loginLog.setSuccess(success ? 1 : 0);
        loginLog.setMessage(message);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
    }

    public PageResult<LoginLog> page(LoginLogQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(), orderBy != null ? orderBy : "id DESC");
        return PageResult.of(loginLogMapper.selectPage(query));
    }

    public LoginLog getById(Long id) {
        return loginLogMapper.selectById(id);
    }

    public void delete(Long id) {
        loginLogMapper.deleteById(id);
    }

    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            loginLogMapper.deleteByIds(ids);
        }
    }

    public void clean(LocalDateTime before) {
        loginLogMapper.deleteBefore(before);
    }

    public List<LoginLog> listForExport(LoginLogQuery query) {
        return loginLogMapper.selectForExport(query);
    }

    public void export(HttpServletResponse response, LoginLogQuery query) {
        List<LoginLogExportRow> rows = loginLogMapper.selectForExport(query).stream()
                .map(this::toExportRow)
                .toList();
        excelService.write(response, "登录日志.xlsx", "登录日志", LoginLogExportRow.class, rows);
    }

    private LoginLogExportRow toExportRow(LoginLog log) {
        LoginLogExportRow row = new LoginLogExportRow();
        row.setUsername(log.getUsername());
        row.setIp(log.getIp());
        row.setBrowser(log.getBrowser());
        row.setOs(log.getOs());
        row.setStatus(log.getSuccess() != null && log.getSuccess() == 1 ? "成功" : "失败");
        row.setMessage(log.getMessage());
        row.setLoginTime(log.getLoginTime());
        return row;
    }
}
