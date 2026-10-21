package com.campus.system.menu.mapper;

import com.campus.system.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<Menu> selectAll();

    List<Menu> selectByUserId(@Param("userId") Long userId);

    List<String> selectPermsByUserId(@Param("userId") Long userId);

    Menu selectById(@Param("id") Long id);

    int insert(Menu menu);

    int updateById(Menu menu);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countChildren(@Param("parentId") Long parentId);

    int countRoleRefs(@Param("menuId") Long menuId);

    int deleteRoleMenuByMenuId(@Param("menuId") Long menuId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
