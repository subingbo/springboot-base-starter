package com.campus.common.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {

    private int total;
    private int success;
    private int failed;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        if (errors.size() < 100) {
            errors.add(error);
        }
    }
}
