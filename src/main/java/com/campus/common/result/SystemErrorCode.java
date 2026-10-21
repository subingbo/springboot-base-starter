package com.campus.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SystemErrorCode implements ErrorCode {

    USERNAME_EXISTS(20001, "用户名已存在", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(20002, "角色不存在", HttpStatus.NOT_FOUND),
    ROLE_CODE_EXISTS(20003, "角色编码已存在", HttpStatus.CONFLICT),
    ROLE_IN_USE(20004, "角色已被用户使用", HttpStatus.CONFLICT),
    DEPT_NOT_FOUND(20005, "部门不存在", HttpStatus.NOT_FOUND),
    DEPT_HAS_CHILDREN(20006, "部门下存在子部门", HttpStatus.CONFLICT),
    DEPT_HAS_USERS(20007, "部门下存在用户", HttpStatus.CONFLICT),
    MENU_NOT_FOUND(20008, "菜单不存在", HttpStatus.NOT_FOUND),
    MENU_HAS_CHILDREN(20009, "菜单下存在子菜单", HttpStatus.CONFLICT),
    MENU_IN_USE(20010, "菜单已被角色引用", HttpStatus.CONFLICT),
    POST_NOT_FOUND(20011, "岗位不存在", HttpStatus.NOT_FOUND),
    POST_CODE_EXISTS(20012, "岗位编码已存在", HttpStatus.CONFLICT),
    POST_IN_USE(20013, "岗位已被用户使用", HttpStatus.CONFLICT),
    DICT_TYPE_EXISTS(20014, "字典类型已存在", HttpStatus.CONFLICT),
    DICT_DATA_EXISTS(20015, "字典数据已存在", HttpStatus.CONFLICT),
    CONFIG_KEY_EXISTS(20016, "参数键名已存在", HttpStatus.CONFLICT),
    USER_NOT_FOUND(20017, "用户不存在", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_SELF(20018, "不能删除当前登录用户", HttpStatus.CONFLICT),
    CANNOT_MODIFY_ADMIN(20019, "不能修改初始管理员的关键信息", HttpStatus.CONFLICT),
    OPTIMISTIC_LOCK(20020, "数据已被修改，请刷新后重试", HttpStatus.CONFLICT),
    INVALID_PARENT(20021, "上级节点不能是自身或子节点", HttpStatus.BAD_REQUEST),
    USER_DISABLED(20022, "账号已禁用", HttpStatus.FORBIDDEN),
    INVALID_ID_LIST(20023, "ID 列表不能为空", HttpStatus.BAD_REQUEST),
    CONFIG_NOT_FOUND(20024, "参数不存在", HttpStatus.NOT_FOUND),
    DICT_TYPE_NOT_FOUND(20025, "字典类型不存在", HttpStatus.NOT_FOUND),
    DICT_DATA_NOT_FOUND(20026, "字典数据不存在", HttpStatus.NOT_FOUND),
    DICT_TYPE_IN_USE(20027, "字典类型下存在数据", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
