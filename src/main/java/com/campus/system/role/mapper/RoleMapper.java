package com.campus.system.role.mapper;

import com.campus.system.role.dto.RoleQuery;
import com.campus.system.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<Role> selectPage(RoleQuery query);

    Role selectById(@Param("id") Long id);

    Role selectByCode(@Param("code") String code);

    List<Role> selectByUserId(@Param("userId") Long userId);

    List<String> selectCodesByUserId(@Param("userId") Long userId);

    Long selectIdByCode(@Param("code") String code);

    List<Role> selectAllEnabled();

    int insert(Role role);

    int updateById(Role role);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int countUsersByRoleId(@Param("roleId") Long roleId);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    int deleteRoleMenus(@Param("roleId") Long roleId);

    int insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    int deleteRoleDepts(@Param("roleId") Long roleId);

    int insertRoleDepts(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
