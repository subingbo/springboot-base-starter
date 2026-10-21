package com.campus.system.dept.service;

import com.campus.common.datascope.DataScopeService;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.SystemErrorCode;
import com.campus.security.user.SecurityUtils;
import com.campus.system.dept.dto.DeptRequest;
import com.campus.system.dept.dto.DeptResponse;
import com.campus.system.dept.entity.Dept;
import com.campus.system.dept.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptMapper deptMapper;
    private final DataScopeService dataScopeService;

    public List<DeptResponse> tree() {
        List<Dept> depts = deptMapper.selectAll();
        Map<Long, DeptResponse> map = new LinkedHashMap<>();
        for (Dept dept : depts) {
            map.put(dept.getId(), DeptResponse.from(dept));
        }
        List<DeptResponse> roots = new ArrayList<>();
        for (Dept dept : depts) {
            DeptResponse response = map.get(dept.getId());
            DeptResponse parent = map.get(dept.getParentId());
            if (parent != null && !dept.getId().equals(dept.getParentId())) {
                parent.getChildren().add(response);
            } else {
                roots.add(response);
            }
        }
        return roots;
    }

    public Dept getById(Long id) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        return dept;
    }

    @Transactional
    public Long create(DeptRequest request) {
        Dept parent = resolveParent(request.getParentId());
        Dept dept = new Dept();
        dept.setParentId(parent == null ? 0L : parent.getId());
        dept.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + parent.getId());
        apply(dept, request);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Transactional
    public void update(Long id, DeptRequest request) {
        Dept dept = getById(id);
        Long newParentId = request.getParentId() == null ? 0L : request.getParentId();
        if (id.equals(newParentId)) {
            throw new BusinessException(SystemErrorCode.INVALID_PARENT);
        }
        Dept parent = resolveParent(newParentId);
        if (parent != null) {
            String ancestors = "," + parent.getAncestors() + ",";
            if (ancestors.contains("," + id + ",")) {
                throw new BusinessException(SystemErrorCode.INVALID_PARENT);
            }
        }
        String oldAncestors = dept.getAncestors();
        String newAncestors = parent == null ? "0" : parent.getAncestors() + "," + parent.getId();
        dept.setParentId(parent == null ? 0L : parent.getId());
        dept.setAncestors(newAncestors);
        apply(dept, request);
        int rows = deptMapper.updateById(dept);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        if (!Objects.equals(oldAncestors, newAncestors)) {
            String oldPrefix = oldAncestors + "," + id + ",";
            String newPrefix = newAncestors + "," + id + ",";
            deptMapper.updateDescendantAncestors(oldPrefix, newPrefix, SecurityUtils.getUserIdOrNull());
        }
        dataScopeService.invalidate();
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        if (deptMapper.countChildren(id) > 0) {
            throw new BusinessException(SystemErrorCode.DEPT_HAS_CHILDREN);
        }
        if (deptMapper.countUsers(id) > 0) {
            throw new BusinessException(SystemErrorCode.DEPT_HAS_USERS);
        }
        int rows = deptMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        dataScopeService.invalidate();
    }

    private Dept resolveParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }
        return getById(parentId);
    }

    private void apply(Dept dept, DeptRequest request) {
        dept.setName(request.getName());
        dept.setLeader(request.getLeader());
        dept.setPhone(request.getPhone());
        dept.setEmail(request.getEmail());
        dept.setSort(request.getSort());
        dept.setStatus(request.getStatus());
        dept.setRemark(request.getRemark());
    }
}
