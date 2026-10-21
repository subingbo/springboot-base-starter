package com.campus.common.page;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;
    private List<T> list;

    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(), info.getPages(), info.getList());
    }

    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(0, pageNum, pageSize, 0, List.of());
    }
}
