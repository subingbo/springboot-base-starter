package com.campus.file.mapper;

import com.campus.file.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysFileMapper {

    int insert(SysFile file);

    SysFile selectById(@Param("id") Long id);

    int updateUrl(@Param("id") Long id, @Param("url") String url);
}
