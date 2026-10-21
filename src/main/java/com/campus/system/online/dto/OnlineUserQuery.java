package com.campus.system.online.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineUserQuery extends PageQuery {

    private String username;
    private String ip;
}
