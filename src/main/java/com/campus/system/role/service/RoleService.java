package com.campus.system.role.service;

import com.campus.auth.service.TokenSessionService;
import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.datascope.DataScopeService;
import com.campus.common.exception.BusinessException;
import com.campus.common.page.PageResult;
import com.campus.common.result.SystemErrorCode;
import com.campus.security.user.SecurityUtils;
import com.campus.system.role.dto.RoleQuery;
import com.campus.system.role.dto.RoleRequest;
import com.campus.system.role.dto.RoleResponse;
import com.campus.system.role.entity.Role;
import com.campus.system.role.mapper.RoleMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final String ADMIN_CODE = "ADMIN";

    private final RoleMapper roleMapper;
    private final TokenSessionService tokenSessionService;
    private final CacheService cacheService;
    private final DataScopeService dataScopeService;

    public PageResult<RoleResponse> page(RoleQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(), orderBy != null ? orderBy : "sort ASC, id ASC");
        List<Role> roles = roleMapper.selectPage(query);
        List<RoleResponse> responses = roles.stream().map(RoleResponse::from).toList();
        PageResult<Role> page = PageResult.of(roles);
        return new PageResult<>(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(), responses);
    }

    public RoleResponse getById(Long id) {
        Role role = requireRole(id);
        RoleResponse response = RoleResponse.from(role);
        response.setMenuIds(roleMapper.selectMenuIdsByRoleId(id));
        response.setDeptIds(roleMapper.selectDeptIdsByRoleId(id));
        return response;
    }

    @Transactional
    public Long create(RoleRequest request) {
        if (roleMapper.countByCode(request.getCode(), null) > 0) {
            throw new BusinessException(SystemErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        apply(role, request);
        roleMapper.insert(role);
        return role.getId();
    }

    @Transactional
    public void update(Long id, RoleRequest request) {
        Role role = requireRole(id);
        if (ADMIN_CODE.equals(role.getCode())) {
            if (!ADMIN_CODE.equals(request.getCode())) {
                throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能修改管理员角色编码");
            }
            if (request.getStatus() != null && request.getStatus() == 0) {
                throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能停用管理员角色");
            }
        }
        if (roleMapper.countByCode(request.getCode(), id) > 0) {
            throw new BusinessException(SystemErrorCode.ROLE_CODE_EXISTS);
        }
        apply(role, request);
        int rows = roleMapper.updateById(role);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evictRoleUsers(id);
        dataScopeService.invalidate();
    }

    @Transactional
    public void delete(Long id) {
        Role role = requireRole(id);
        if (ADMIN_CODE.equals(role.getCode())) {
            throw new BusinessException(SystemErrorCode.CANNOT_MODIFY_ADMIN, "不能删除管理员角色");
        }
        if (roleMapper.countUsersByRoleId(id) > 0) {
            throw new BusinessException(SystemErrorCode.ROLE_IN_USE);
        }
        roleMapper.deleteRoleMenus(id);
        roleMapper.deleteRoleDepts(id);
        int rows = roleMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evictRoleUsers(id);
        dataScopeService.invalidate();
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        requireRole(roleId);
        roleMapper.deleteRoleMenus(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMapper.insertRoleMenus(roleId, menuIds);
        }
        evictRoleUsers(roleId);
    }

    @Transactional
    public void assignDepts(Long roleId, List<Long> deptIds) {
        Role role = requireRole(roleId);
        roleMapper.deleteRoleDepts(roleId);
        if (deptIds != null && !deptIds.isEmpty()) {
            roleMapper.insertRoleDepts(roleId, deptIds);
        }
        role.setDataScope(2);
        int rows = roleMapper.updateById(role);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evictRoleUsers(roleId);
        dataScopeService.invalidate();
    }

    private Role requireRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void apply(Role role, RoleRequest request) {
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setSort(request.getSort());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
    }

    private void evictRoleUsers(Long roleId) {
        List<Long> userIds = roleMapper.selectUserIdsByRoleId(roleId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        tokenSessionService.evictLoginUsers(userIds);
        userIds.forEach(userId -> cacheService.evict(CacheKeys.routes(userId)));
    }
}
