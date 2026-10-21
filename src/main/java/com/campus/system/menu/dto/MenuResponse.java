package com.campus.system.menu.dto;

import com.campus.system.menu.entity.Menu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuResponse {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private String type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private List<MenuResponse> children = new ArrayList<>();

    public static MenuResponse from(Menu menu) {
        MenuResponse response = new MenuResponse();
        response.setId(menu.getId());
        response.setParentId(menu.getParentId());
        response.setName(menu.getName());
        response.setPath(menu.getPath());
        response.setComponent(menu.getComponent());
        response.setPerms(menu.getPerms());
        response.setType(menu.getType());
        response.setIcon(menu.getIcon());
        response.setSort(menu.getSort());
        response.setVisible(menu.getVisible());
        response.setStatus(menu.getStatus());
        return response;
    }
}
