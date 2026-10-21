package com.campus.common.datascope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DataScopeRule> rules = new ArrayList<>();
}
