package com.campus.system.dept.mapper;

import com.campus.system.dept.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeptMapper {

    List<Dept> selectAll();

    Dept selectById(@Param("id") Long id);

    int insert(Dept dept);

    int updateById(Dept dept);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countChildren(@Param("parentId") Long parentId);

    int countUsers(@Param("deptId") Long deptId);

    int updateDescendantAncestors(@Param("oldPrefix") String oldPrefix,
                                  @Param("newPrefix") String newPrefix,
                                  @Param("updateBy") Long updateBy);

    List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);
}
