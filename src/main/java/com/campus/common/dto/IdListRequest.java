package com.campus.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IdListRequest {

    @NotNull(message = "ID 列表不能为空")
    private List<Long> ids;
}
