package com.campus.common.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.CommonErrorCode;

import java.util.ArrayList;
import java.util.List;

public class ExcelReadListener<T> extends AnalysisEventListener<T> {

    private final int maxRows;
    private final List<T> data = new ArrayList<>();

    public ExcelReadListener(int maxRows) {
        this.maxRows = Math.max(1, maxRows);
    }

    @Override
    public void invoke(T row, AnalysisContext context) {
        if (data.size() >= maxRows) {
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT,
                    "导入数据不能超过 " + maxRows + " 行");
        }
        data.add(row);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // no-op
    }

    public List<T> getData() {
        return data;
    }
}
