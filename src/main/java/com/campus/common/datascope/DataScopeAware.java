package com.campus.common.datascope;

public interface DataScopeAware {

    DataScopeFilter getDataScope();

    void setDataScope(DataScopeFilter dataScope);
}
