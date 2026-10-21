package com.campus.system.config.mapper;

import com.campus.system.config.dto.ConfigQuery;
import com.campus.system.config.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigMapper {

    List<SysConfig> selectPage(ConfigQuery query);

    SysConfig selectById(@Param("id") Long id);

    SysConfig selectByKey(@Param("configKey") String configKey);

    int insert(SysConfig config);

    int updateById(SysConfig config);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByKey(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);
}
