package com.campus.system.dict.service;

import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.exception.BusinessException;
import com.campus.common.excel.DictTranslator;
import com.campus.common.page.PageResult;
import com.campus.common.result.SystemErrorCode;
import com.campus.security.user.SecurityUtils;
import com.campus.system.dict.dto.DictDataRequest;
import com.campus.system.dict.dto.DictTypeQuery;
import com.campus.system.dict.dto.DictTypeRequest;
import com.campus.system.dict.entity.DictData;
import com.campus.system.dict.entity.DictType;
import com.campus.system.dict.mapper.DictDataMapper;
import com.campus.system.dict.mapper.DictTypeMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DictService implements DictTranslator {

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final CacheService cacheService;

    public PageResult<DictType> pageTypes(DictTypeQuery query) {
        String orderBy = query.toOrderBy();
        PageHelper.startPage(query.getPageNum(), query.getPageSize(),
                orderBy != null ? orderBy : "id DESC");
        return PageResult.of(dictTypeMapper.selectPage(query));
    }

    public List<DictType> enabledTypes() {
        return dictTypeMapper.selectAllEnabled();
    }

    public DictType getType(Long id) {
        DictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_NOT_FOUND);
        }
        return type;
    }

    @Transactional
    public Long createType(DictTypeRequest request) {
        if (dictTypeMapper.countByType(request.getDictType(), null) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_EXISTS);
        }
        DictType type = new DictType();
        type.setDictName(request.getDictName());
        type.setDictType(request.getDictType());
        type.setStatus(request.getStatus());
        type.setRemark(request.getRemark());
        dictTypeMapper.insert(type);
        evict(type.getDictType());
        return type.getId();
    }

    @Transactional
    public void updateType(Long id, DictTypeRequest request) {
        DictType type = getType(id);
        if (dictTypeMapper.countByType(request.getDictType(), id) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_EXISTS);
        }
        String oldType = type.getDictType();
        if (!Objects.equals(oldType, request.getDictType())
                && dictDataMapper.countByType(oldType) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_IN_USE, "请先删除该字典下的数据");
        }
        type.setDictName(request.getDictName());
        type.setDictType(request.getDictType());
        type.setStatus(request.getStatus());
        type.setRemark(request.getRemark());
        int rows = dictTypeMapper.updateById(type);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(oldType);
        evict(type.getDictType());
    }

    @Transactional
    public void deleteType(Long id) {
        DictType type = getType(id);
        if (dictDataMapper.countByType(type.getDictType()) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_IN_USE);
        }
        int rows = dictTypeMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(type.getDictType());
    }

    public List<DictData> listData(String dictType) {
        return dictDataMapper.selectByType(dictType);
    }

    @SuppressWarnings("unchecked")
    public List<DictData> getEnabledData(String dictType) {
        return cacheService.getOrLoad(CacheKeys.dict(dictType), CACHE_TTL,
                () -> dictDataMapper.selectByTypeEnabled(dictType));
    }

    public String translate(String dictType, Object value) {
        if (value == null || dictType == null || dictType.isBlank()) {
            return value == null ? null : String.valueOf(value);
        }
        String raw = String.valueOf(value);
        for (DictData data : getEnabledData(dictType)) {
            if (raw.equals(data.getDictValue())) {
                return data.getDictLabel();
            }
        }
        return raw;
    }

    public DictData getData(Long id) {
        DictData data = dictDataMapper.selectById(id);
        if (data == null) {
            throw new BusinessException(SystemErrorCode.DICT_DATA_NOT_FOUND);
        }
        return data;
    }

    @Transactional
    public Long createData(DictDataRequest request) {
        requireType(request.getDictType());
        if (dictDataMapper.countByTypeAndValue(request.getDictType(), request.getDictValue(), null) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_DATA_EXISTS);
        }
        DictData data = new DictData();
        apply(data, request);
        dictDataMapper.insert(data);
        evict(data.getDictType());
        return data.getId();
    }

    @Transactional
    public void updateData(Long id, DictDataRequest request) {
        DictData data = getData(id);
        requireType(request.getDictType());
        if (dictDataMapper.countByTypeAndValue(request.getDictType(), request.getDictValue(), id) > 0) {
            throw new BusinessException(SystemErrorCode.DICT_DATA_EXISTS);
        }
        String oldType = data.getDictType();
        apply(data, request);
        int rows = dictDataMapper.updateById(data);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(oldType);
        evict(data.getDictType());
    }

    @Transactional
    public void deleteData(Long id) {
        DictData data = getData(id);
        int rows = dictDataMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evict(data.getDictType());
    }

    private void requireType(String dictType) {
        if (dictTypeMapper.selectByType(dictType) == null) {
            throw new BusinessException(SystemErrorCode.DICT_TYPE_NOT_FOUND);
        }
    }

    private void apply(DictData data, DictDataRequest request) {
        data.setDictType(request.getDictType());
        data.setDictLabel(request.getDictLabel());
        data.setDictValue(request.getDictValue());
        data.setDictSort(request.getDictSort());
        data.setCssClass(request.getCssClass());
        data.setIsDefault(request.getIsDefault());
        data.setStatus(request.getStatus());
        data.setRemark(request.getRemark());
    }

    private void evict(String dictType) {
        if (dictType != null) {
            cacheService.evict(CacheKeys.dict(dictType));
        }
    }
}
