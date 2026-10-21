package com.campus.system.log.mapper;

import com.campus.system.log.dto.OperLogQuery;
import com.campus.system.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperLogMapper {

    int insert(OperationLog operLog);

    List<OperationLog> selectPage(OperLogQuery query);

    OperationLog selectById(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    int deleteByIds(@Param("ids") List<Long> ids);

    int deleteBefore(@Param("before") LocalDateTime before);

    List<OperationLog> selectForExport(OperLogQuery query);
}
