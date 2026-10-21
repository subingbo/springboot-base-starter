package com.campus.system.user.service;

import com.campus.auth.service.PasswordPolicy;
import com.campus.auth.service.TokenSessionService;
import com.campus.common.annotation.DataScope;
import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.datascope.DataScopeService;
import com.campus.common.excel.ExcelService;
import com.campus.common.excel.ImportResult;
import com.campus.common.exception.BusinessException;
import com.campus.common.page.PageResult;
import com.campus.common.result.AuthErrorCode;
import com.campus.common.result.CommonErrorCode;
import com.campus.common.result.SystemErrorCode;
import com.campus.config.FileProperties;
import com.campus.file.util.FileTypeUtils;
import com.campus.security.user.SecurityUtils;
import com.campus.system.config.service.SysConfigService;
import com.campus.system.dept.mapper.DeptMapper;
import com.campus.system.post.mapper.PostMapper;
import com.campus.system.role.mapper.RoleMapper;
import com.campus.system.user.dto.UserCreateRequest;
import com.campus.system.user.dto.UserExportRow;
import com.campus.system.user.dto.UserImportRow;
import com.campus.system.user.dto.UserQuery;
import com.campus.system.user.dto.UserResponse;
import com.campus.system.user.dto.UserUpdateRequest;
import com.campus.system.user.entity.User;
import com.campus.system.user.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private static final String DEFAULT_ROLE_CODE = "USER";
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PostMapper postMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final TokenSessionService tokenSessionService;
    private final CacheService cacheService;
    private final DataScopeService dataScopeService;
    private final ExcelService excelService;
    private final FileProperties fileProperties;
    private final SysConfigService sysConfigService;

    @DataScope
    public PageResult<UserResponse> page(UserQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(), orderBy != null ? orderBy : "u.id DESC");
        List<User> users = userMapper.selectPage(query);
        List<UserResponse> responses = users.stream().map(UserResponse::from).toList();
        PageResult<User> page = PageResult.of(users);
        return new PageResult<>(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(), responses);
    }

    public UserResponse getById(Long id) {
        User user = requireUser(id);
        UserResponse response = UserResponse.from(user);
        response.setRoleIds(userMapper.selectRoleIdsByUserId(id));
        response.setPostIds(userMapper.selectPostIdsByUserId(id));
        return response;
    }

    @Transactional
    public Long create(UserCreateRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(AuthErrorCode.USERNAME_EXISTS);
        }
        if (request.getDeptId() != null && deptMapper.selectById(request.getDeptId()) == null) {
            throw new BusinessException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        passwordPolicy.validate(request.getPassword(), request.getUsername());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername() : request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSex(request.getSex() == null ? 0 : request.getSex());
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setTokenVersion(0);
        user.setRemark(request.getRemark());
        userMapper.insert(user);

        List<Long> roleIds = request.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            Long defaultRoleId = roleMapper.selectIdByCode(DEFAULT_ROLE_CODE);
            roleIds = defaultRoleId == null ? List.of() : List.of(defaultRoleId);
        }
        replaceRoles(user.getId(), roleIds);
        if (request.getPostIds() != null && !request.getPostIds().isEmpty()) {
            validatePosts(request.getPostIds());
            userMapper.insertUserPosts(user.getId(), request.getPostIds());
        }
        return user.getId();
    }

    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        User user = requireUser(id);
        if (id == 1L && request.getStatus() != null && request.getStatus() == 0) {
            throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能停用初始管理员");
        }
        if (request.getDeptId() != null && deptMapper.selectById(request.getDeptId()) == null) {
            throw new BusinessException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }
        user.setVersion(request.getVersion());
        int rows = userMapper.updateById(user);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }

        if (request.getRoleIds() != null) {
            if (id == 1L) {
                Long adminRoleId = roleMapper.selectIdByCode(ADMIN_ROLE_CODE);
                if (adminRoleId == null || !request.getRoleIds().contains(adminRoleId)) {
                    throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能移除初始管理员角色");
                }
            }
            replaceRoles(id, request.getRoleIds());
        }
        if (request.getPostIds() != null) {
            validatePosts(request.getPostIds());
            userMapper.deleteUserPosts(id);
            if (!request.getPostIds().isEmpty()) {
                userMapper.insertUserPosts(id, request.getPostIds());
            }
        }

        evictUser(id);
        if (request.getStatus() != null && request.getStatus() == 0) {
            tokenSessionService.revokeUserSessions(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (Objects.equals(id, SecurityUtils.getUserIdOrNull())) {
            throw new BusinessException(SystemErrorCode.CANNOT_DELETE_SELF);
        }
        if (id == 1L) {
            throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能删除初始管理员");
        }
        requireUser(id);
        userMapper.deleteUserRoles(id);
        userMapper.deleteUserPosts(id);
        int rows = userMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        userMapper.incrementTokenVersion(id);
        tokenSessionService.revokeUserSessions(id);
        evictUser(id);
    }

    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (status != null && status == 0) {
            if (id == 1L) {
                throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能停用初始管理员");
            }
            if (Objects.equals(id, SecurityUtils.getUserIdOrNull())) {
                throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能停用当前登录用户");
            }
        }
        User user = requireUser(id);
        user.setStatus(status);
        int rows = userMapper.updateById(user);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        if (status != null && status == 0) {
            tokenSessionService.revokeUserSessions(id);
        }
        evictUser(id);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = requireUser(id);
        passwordPolicy.validate(newPassword, user.getUsername());
        int rows = userMapper.updatePassword(id, passwordEncoder.encode(newPassword),
                SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        tokenSessionService.revokeUserSessions(id);
        tokenSessionService.evictLoginUser(id);
    }

    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        requireUser(id);
        if (id == 1L) {
            Long adminRoleId = roleMapper.selectIdByCode(ADMIN_ROLE_CODE);
            if (adminRoleId == null || roleIds == null || !roleIds.contains(adminRoleId)) {
                throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能移除初始管理员角色");
            }
        }
        replaceRoles(id, roleIds);
        evictUser(id);
    }

    @Transactional
    public void assignPosts(Long id, List<Long> postIds) {
        requireUser(id);
        validatePosts(postIds);
        userMapper.deleteUserPosts(id);
        if (postIds != null && !postIds.isEmpty()) {
            userMapper.insertUserPosts(id, postIds);
        }
    }

    @DataScope
    public void exportUsers(HttpServletResponse response, UserQuery query) {
        AtomicLong lastId = new AtomicLong(0);
        excelService.writeBatches(response, "用户数据.xlsx", "用户", UserExportRow.class, page -> {
            query.setLastId(lastId.get());
            List<User> users = userMapper.selectExportRows(query);
            if (users.isEmpty()) {
                return List.of();
            }
            lastId.set(users.get(users.size() - 1).getId());
            return users.stream().map(this::toExportRow).toList();
        }, 1000);
    }

    public void writeImportTemplate(HttpServletResponse response) {
        excelService.writeTemplate(response, "用户导入模板.xlsx", "用户", UserImportRow.class);
    }

    public ImportResult importUsers(MultipartFile file) {
        int maxMb = sysConfigService.getInt("sys.file.maxSizeMb", fileProperties.getMaxSizeMb());
        FileTypeUtils.validateExcel(file, maxMb);
        List<UserImportRow> rows = excelService.read(file, UserImportRow.class,
                fileProperties.getImportMaxRows());
        ImportResult result = new ImportResult();
        result.setTotal(rows.size());
        int success = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                createFromImport(rows.get(i));
                success++;
            } catch (Exception ex) {
                failed++;
                result.addError("第 " + (i + 2) + " 行：" + ex.getMessage());
            }
        }
        result.setSuccess(success);
        result.setFailed(failed);
        return result;
    }

    private void createFromImport(UserImportRow row) {
        if (row == null) {
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT, "空行");
        }
        String username = trim(row.getUsername());
        String password = row.getPassword();
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT, "用户名格式不正确");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT, "密码不能为空");
        }
        if (userMapper.countByUsername(username) > 0) {
            throw new BusinessException(AuthErrorCode.USERNAME_EXISTS);
        }
        passwordPolicy.validate(password, username);
        if (row.getDeptId() != null && deptMapper.selectById(row.getDeptId()) == null) {
            throw new BusinessException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        Integer status = row.getStatus() == null ? 1 : row.getStatus();
        if (status != 0 && status != 1) {
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT, "状态只能是 0 或 1");
        }
        List<Long> roleIds = resolveRoleIds(row.getRoleCodes());
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(row.getNickname() == null || row.getNickname().isBlank()
                ? username : row.getNickname().trim());
        user.setEmail(trim(row.getEmail()));
        user.setPhone(trim(row.getPhone()));
        user.setDeptId(row.getDeptId());
        user.setSex(0);
        user.setStatus(status);
        user.setTokenVersion(0);
        userMapper.insert(user);
        if (!roleIds.isEmpty()) {
            userMapper.insertUserRoles(user.getId(), roleIds);
        }
    }

    private List<Long> resolveRoleIds(String roleCodes) {
        List<Long> roleIds = new ArrayList<>();
        if (roleCodes == null || roleCodes.isBlank()) {
            addDefaultRole(roleIds);
            return roleIds;
        }
        for (String code : roleCodes.split("[,，]")) {
            String trimmed = code.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Long roleId = roleMapper.selectIdByCode(trimmed.toUpperCase(Locale.ROOT));
            if (roleId == null) {
                throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND, "角色不存在: " + trimmed);
            }
            roleIds.add(roleId);
        }
        if (roleIds.isEmpty()) {
            addDefaultRole(roleIds);
        }
        return roleIds;
    }

    private void addDefaultRole(List<Long> roleIds) {
        Long defaultRoleId = roleMapper.selectIdByCode(DEFAULT_ROLE_CODE);
        if (defaultRoleId != null) {
            roleIds.add(defaultRoleId);
        }
    }

    private UserExportRow toExportRow(User user) {
        UserExportRow row = new UserExportRow();
        row.setUsername(user.getUsername());
        row.setNickname(user.getNickname());
        row.setDeptName(user.getDeptName());
        row.setEmail(user.getEmail());
        row.setPhone(user.getPhone());
        row.setSex(user.getSex());
        row.setStatus(user.getStatus());
        row.setCreateTime(user.getCreateTime());
        return row;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRoles(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            if (roleMapper.selectById(roleId) == null) {
                throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
            }
        }
        userMapper.insertUserRoles(userId, roleIds);
    }

    private void validatePosts(List<Long> postIds) {
        if (postIds == null) {
            return;
        }
        for (Long postId : postIds) {
            if (postMapper.selectById(postId) == null) {
                throw new BusinessException(SystemErrorCode.POST_NOT_FOUND);
            }
        }
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void evictUser(Long userId) {
        tokenSessionService.evictLoginUser(userId);
        cacheService.evict(CacheKeys.routes(userId));
        dataScopeService.invalidate();
    }
}
