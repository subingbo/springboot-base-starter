package com.campus.common.datascope;

import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.enums.DataScopeType;
import com.campus.system.dept.entity.Dept;
import com.campus.system.dept.mapper.DeptMapper;
import com.campus.system.role.entity.Role;
import com.campus.system.role.mapper.RoleMapper;
import com.campus.system.user.entity.User;
import com.campus.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataScopeService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    /**
     * 返回 null 表示不限制（ALL 或管理员）。
     */
    public DataScopeFilter buildFilter(Long userId) {
        long version = currentVersion();
        return cacheService.getOrLoad(CacheKeys.dataScope(version, userId), CACHE_TTL, () -> load(userId));
    }

    public void invalidate() {
        cacheService.increment(CacheKeys.dataScopeVersion(), Duration.ofDays(30));
    }

    private DataScopeFilter load(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return selfOnly(userId);
        }
        List<Role> roles = roleMapper.selectByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return selfOnly(userId);
        }
        List<DataScopeRule> rules = new ArrayList<>();
        for (Role role : roles) {
            DataScopeType scope = DataScopeType.fromCode(role.getDataScope());
            switch (scope) {
                case ALL -> {
                    return null;
                }
                case CUSTOM -> {
                    List<Long> deptIds = roleMapper.selectDeptIdsByRoleId(role.getId());
                    rules.add(DataScopeRule.builder()
                            .type(deptIds == null || deptIds.isEmpty() ? "NONE" : "CUSTOM")
                            .deptIds(deptIds)
                            .build());
                }
                case DEPT -> rules.add(DataScopeRule.builder()
                        .type(user.getDeptId() == null ? "NONE" : "DEPT")
                        .deptId(user.getDeptId())
                        .build());
                case DEPT_AND_CHILD -> {
                    if (user.getDeptId() == null) {
                        rules.add(DataScopeRule.builder().type("NONE").build());
                    } else {
                        Dept dept = deptMapper.selectById(user.getDeptId());
                        if (dept == null) {
                            rules.add(DataScopeRule.builder().type("NONE").build());
                        } else {
                            String base = dept.getAncestors() == null || dept.getAncestors().isBlank()
                                    ? "0" : dept.getAncestors();
                            rules.add(DataScopeRule.builder()
                                    .type("DEPT_AND_CHILD")
                                    .deptId(dept.getId())
                                    .ancestorsPrefix(base + "," + dept.getId() + ",")
                                    .build());
                        }
                    }
                }
                case SELF -> rules.add(DataScopeRule.builder()
                        .type("SELF")
                        .userId(userId)
                        .build());
            }
        }
        if (rules.isEmpty()) {
            return selfOnly(userId);
        }
        return new DataScopeFilter(rules);
    }

    private DataScopeFilter selfOnly(Long userId) {
        return new DataScopeFilter(List.of(DataScopeRule.builder()
                .type("SELF")
                .userId(userId)
                .build()));
    }

    private long currentVersion() {
        String value = cacheService.getString(CacheKeys.dataScopeVersion());
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
