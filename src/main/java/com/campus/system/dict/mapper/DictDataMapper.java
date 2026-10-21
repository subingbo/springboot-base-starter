package com.campus.system.dict.mapper;

import com.campus.system.dict.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictDataMapper {

    List<DictData> selectByType(@Param("dictType") String dictType);

    List<DictData> selectByTypeEnabled(@Param("dictType") String dictType);

    DictData selectById(@Param("id") Long id);

    int insert(DictData dictData);

    int updateById(DictData dictData);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByTypeAndValue(@Param("dictType") String dictType,
                            @Param("dictValue") String dictValue,
                            @Param("excludeId") Long excludeId);

    int countByType(@Param("dictType") String dictType);
}
