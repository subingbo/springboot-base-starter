package com.campus.system.post.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.page.PageResult;
import com.campus.common.result.SystemErrorCode;
import com.campus.security.user.SecurityUtils;
import com.campus.system.post.dto.PostQuery;
import com.campus.system.post.dto.PostRequest;
import com.campus.system.post.entity.Post;
import com.campus.system.post.mapper.PostMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    public PageResult<Post> page(PostQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(),
                orderBy != null ? orderBy : "sort ASC, id ASC");
        return PageResult.of(postMapper.selectPage(query));
    }

    public List<Post> enabled() {
        return postMapper.selectAllEnabled();
    }

    public Post getById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(SystemErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    @Transactional
    public Long create(PostRequest request) {
        if (postMapper.countByCode(request.getCode(), null) > 0) {
            throw new BusinessException(SystemErrorCode.POST_CODE_EXISTS);
        }
        Post post = new Post();
        apply(post, request);
        postMapper.insert(post);
        return post.getId();
    }

    @Transactional
    public void update(Long id, PostRequest request) {
        Post post = getById(id);
        if (postMapper.countByCode(request.getCode(), id) > 0) {
            throw new BusinessException(SystemErrorCode.POST_CODE_EXISTS);
        }
        apply(post, request);
        int rows = postMapper.updateById(post);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        if (postMapper.countUsers(id) > 0) {
            throw new BusinessException(SystemErrorCode.POST_IN_USE);
        }
        int rows = postMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
    }

    private void apply(Post post, PostRequest request) {
        post.setCode(request.getCode());
        post.setName(request.getName());
        post.setSort(request.getSort());
        post.setStatus(request.getStatus());
        post.setRemark(request.getRemark());
    }
}
