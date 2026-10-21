package com.campus.system.dict.mapper;

import com.campus.system.dict.dto.DictTypeQuery;
import com.campus.system.dict.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictTypeMapper {

    List<DictType> selectPage(DictTypeQuery query);

    List<DictType> selectAllEnabled();

    DictType selectById(@Param("id") Long id);

    DictType selectByType(@Param("dictType") String dictType);

    int insert(DictType dictType);

    int updateById(DictType dictType);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByType(@Param("dictType") String dictType, @Param("excludeId") Long excludeId);
}
