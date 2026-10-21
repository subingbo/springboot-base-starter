package com.campus.common.validation;

import jakarta.validation.groups.Default;

/**
 * 继承 Default，保证未显式分组的约束在新增时同样生效。
 */
public interface Create extends Default {
}
