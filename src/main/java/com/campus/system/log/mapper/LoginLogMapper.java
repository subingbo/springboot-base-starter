package com.campus.system.log.mapper;

import com.campus.system.log.dto.LoginLogQuery;
import com.campus.system.log.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoginLogMapper {

    int insert(LoginLog loginLog);

    List<LoginLog> selectPage(LoginLogQuery query);

    LoginLog selectById(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    int deleteByIds(@Param("ids") List<Long> ids);

    int deleteBefore(@Param("before") LocalDateTime before);

    List<LoginLog> selectForExport(LoginLogQuery query);
}
