package com.campus.system.user.mapper;

import com.campus.system.user.dto.UserQuery;
import com.campus.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);

    String selectPasswordById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int insert(User user);

    int insertBatch(@Param("list") List<User> users);

    int updateById(User user);

    int updatePassword(@Param("id") Long id,
                       @Param("password") String password,
                       @Param("updateBy") Long updateBy);

    int incrementTokenVersion(@Param("id") Long id);

    int updateLoginInfo(@Param("id") Long id,
                        @Param("loginIp") String loginIp,
                        @Param("loginTime") LocalDateTime loginTime);

    int updateAvatar(@Param("id") Long id,
                     @Param("avatar") String avatar,
                     @Param("updateBy") Long updateBy);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int deleteUserRoles(@Param("userId") Long userId);

    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    int deleteUserPosts(@Param("userId") Long userId);

    int insertUserPosts(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    List<Long> selectPostIdsByUserId(@Param("userId") Long userId);

    List<User> selectPage(UserQuery query);

    List<User> selectExportRows(UserQuery query);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);

    int countByDeptId(@Param("deptId") Long deptId);
}
