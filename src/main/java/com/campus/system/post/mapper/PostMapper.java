package com.campus.system.post.mapper;

import com.campus.system.post.dto.PostQuery;
import com.campus.system.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    List<Post> selectPage(PostQuery query);

    List<Post> selectAllEnabled();

    Post selectById(@Param("id") Long id);

    int insert(Post post);

    int updateById(Post post);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int countUsers(@Param("postId") Long postId);
}
