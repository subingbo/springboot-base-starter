package com.campus.system.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private String name;
    private String path;
    private String component;
    private String redirect;
    private String title;
    private String icon;
    private Boolean hidden;
    @Builder.Default
    private List<RouteResponse> children = new ArrayList<>();
}
