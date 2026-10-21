package com.campus.system.menu.service;

import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.SystemErrorCode;
import com.campus.config.SecurityProperties;
import com.campus.security.user.SecurityUtils;
import com.campus.system.menu.dto.MenuRequest;
import com.campus.system.menu.dto.MenuResponse;
import com.campus.system.menu.dto.RouteResponse;
import com.campus.system.menu.entity.Menu;
import com.campus.system.menu.mapper.MenuMapper;
import com.campus.system.role.mapper.RoleMapper;
import com.campus.auth.service.TokenSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuService {

    private static final Duration ROUTE_CACHE_TTL = Duration.ofMinutes(10);

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final TokenSessionService tokenSessionService;
    private final CacheService cacheService;
    private final SecurityProperties securityProperties;

    public List<MenuResponse> tree() {
        List<Menu> menus = menuMapper.selectAll();
        Map<Long, MenuResponse> map = new LinkedHashMap<>();
        for (Menu menu : menus) {
            map.put(menu.getId(), MenuResponse.from(menu));
        }
        List<MenuResponse> roots = new ArrayList<>();
        for (Menu menu : menus) {
            MenuResponse response = map.get(menu.getId());
            MenuResponse parent = map.get(menu.getParentId());
            if (parent != null && !menu.getId().equals(menu.getParentId())) {
                parent.getChildren().add(response);
            } else {
                roots.add(response);
            }
        }
        return roots;
    }

    @SuppressWarnings("unchecked")
    public List<RouteResponse> routes(Long userId) {
        String cacheKey = CacheKeys.routes(userId);
        List<RouteResponse> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }
        List<Menu> menus = SecurityUtils.isAdmin(securityProperties.getAdminRoleCode())
                ? menuMapper.selectAll()
                : menuMapper.selectByUserId(userId);
        List<Menu> routeMenus = menus.stream()
                .filter(menu -> "M".equals(menu.getType()) || "C".equals(menu.getType()))
                .filter(menu -> menu.getStatus() == null || menu.getStatus() == 1)
                .sorted(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Menu::getId))
                .toList();
        Map<Long, RouteResponse> map = new LinkedHashMap<>();
        for (Menu menu : routeMenus) {
            map.put(menu.getId(), toRoute(menu));
        }
        List<RouteResponse> roots = new ArrayList<>();
        for (Menu menu : routeMenus) {
            RouteResponse route = map.get(menu.getId());
            RouteResponse parent = map.get(menu.getParentId());
            if (parent != null && !menu.getId().equals(menu.getParentId())) {
                parent.getChildren().add(route);
            } else {
                roots.add(route);
            }
        }
        cacheService.putWithJitter(cacheKey, roots, ROUTE_CACHE_TTL);
        return roots;
    }

    public Menu getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(SystemErrorCode.MENU_NOT_FOUND);
        }
        return menu;
    }

    @Transactional
    public Long create(MenuRequest request) {
        validateParent(request.getParentId(), request.getType(), null);
        Menu menu = new Menu();
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        apply(menu, request);
        menuMapper.insert(menu);
        evictAffectedUsers(menu.getId());
        return menu.getId();
    }

    @Transactional
    public void update(Long id, MenuRequest request) {
        Menu menu = getById(id);
        validateParent(request.getParentId(), request.getType(), id);
        if (request.getParentId() != null) {
            menu.setParentId(request.getParentId());
        }
        apply(menu, request);
        int rows = menuMapper.updateById(menu);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evictAffectedUsers(id);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        if (menuMapper.countChildren(id) > 0) {
            throw new BusinessException(SystemErrorCode.MENU_HAS_CHILDREN);
        }
        if (menuMapper.countRoleRefs(id) > 0) {
            throw new BusinessException(SystemErrorCode.MENU_IN_USE);
        }
        int rows = menuMapper.logicalDelete(id, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        evictAffectedUsers(id);
    }

    private void apply(Menu menu, MenuRequest request) {
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPerms(request.getPerms());
        menu.setType(request.getType());
        menu.setIcon(request.getIcon());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
    }

    private void validateParent(Long parentId, String type, Long selfId) {
        if (parentId == null || parentId == 0L) {
            if ("F".equals(type)) {
                throw new BusinessException(SystemErrorCode.INVALID_PARENT, "按钮必须挂在菜单下");
            }
            return;
        }
        Menu parent = getById(parentId);
        if ("F".equals(parent.getType())) {
            throw new BusinessException(SystemErrorCode.INVALID_PARENT, "按钮下不能创建子节点");
        }
        if ("F".equals(type) && !"C".equals(parent.getType())) {
            throw new BusinessException(SystemErrorCode.INVALID_PARENT, "按钮只能挂在菜单下");
        }
        if (selfId != null) {
            Long cursor = parent.getId();
            int depth = 0;
            while (cursor != null && cursor != 0 && depth++ < 100) {
                if (cursor.equals(selfId)) {
                    throw new BusinessException(SystemErrorCode.INVALID_PARENT);
                }
                Menu current = menuMapper.selectById(cursor);
                cursor = current == null ? null : current.getParentId();
            }
        }
    }

    private RouteResponse toRoute(Menu menu) {
        return RouteResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .title(menu.getName())
                .icon(menu.getIcon())
                .hidden(menu.getVisible() != null && menu.getVisible() == 0)
                .build();
    }

    private void evictAffectedUsers(Long menuId) {
        List<Long> roleIds = roleMapper.selectRoleIdsByMenuId(menuId);
        Set<Long> userIds = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            userIds.addAll(roleMapper.selectUserIdsByRoleId(roleId));
        }
        if (!userIds.isEmpty()) {
            tokenSessionService.evictLoginUsers(userIds);
            userIds.forEach(userId -> cacheService.evict(CacheKeys.routes(userId)));
        }
    }
}
