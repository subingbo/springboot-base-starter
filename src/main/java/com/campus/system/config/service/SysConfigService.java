package com.campus.system.config.service;

import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.exception.BusinessException;
import com.campus.common.page.PageResult;
import com.campus.common.result.SystemErrorCode;
import com.campus.system.config.dto.ConfigQuery;
import com.campus.system.config.dto.ConfigRequest;
import com.campus.system.config.entity.SysConfig;
import com.campus.system.config.mapper.SysConfigMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final SysConfigMapper configMapper;
    private final CacheService cacheService;

    public PageResult<SysConfig> page(ConfigQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(), orderBy != null ? orderBy : "id DESC");
        return PageResult.of(configMapper.selectPage(query));
    }

    public SysConfig getById(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(SystemErrorCode.CONFIG_NOT_FOUND);
        }
        return config;
    }

    @Transactional
    public Long create(ConfigRequest request) {
        if (configMapper.countByKey(request.getConfigKey(), null) > 0) {
            throw new BusinessException(SystemErrorCode.CONFIG_KEY_EXISTS);
        }
        SysConfig config = new SysConfig();
        config.setConfigName(request.getConfigName());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setRemark(request.getRemark());
        configMapper.insert(config);
        evict(config.getConfigKey());
        return config.getId();
    }

    @Transactional
    public void update(Long id, ConfigRequest request) {
        SysConfig config = getById(id);
        if (configMapper.countByKey(request.getConfigKey(), id) > 0) {
            throw new BusinessException(SystemErrorCode.CONFIG_KEY_EXISTS);
        }
        String oldKey = config.getConfigKey();
        config.setConfigName(request.getConfigName());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setRemark(request.getRemark());
        int rows = configMapper.updateById(config);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(oldKey);
        evict(config.getConfigKey());
    }

    @Transactional
    public void delete(Long id) {
        SysConfig config = getById(id);
        int rows = configMapper.logicalDelete(id, null);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(config.getConfigKey());
    }

    public String getString(String key, String defaultValue) {
        String cacheKey = CacheKeys.config(key);
        String cached = cacheService.get(cacheKey, String.class);
        if (cached != null) {
            return cached;
        }
        SysConfig config = configMapper.selectByKey(key);
        String value = config != null ? config.getConfigValue() : defaultValue;
        if (value != null) {
            cacheService.putWithJitter(cacheKey, value, CACHE_TTL);
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        String value = getString(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, null);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public void evict(String key) {
        cacheService.evict(CacheKeys.config(key));
    }
}
