# Campus Server

一个可复用的 Spring Boot 后端脚手架，覆盖统一接口层、认证会话、RBAC 与数据权限、系统管理、审计日志、缓存、文件上传安全和 Excel 导入导出。

- Java 17、Spring Boot 3.5、Maven
- Spring Security 6 + JWT 双令牌（令牌家族、轮换、重放检测）
- 原生 MyBatis XML + PageHelper（不使用 MyBatis-Plus）
- MySQL 8、Redis 7、HikariCP
- Knife4j / OpenAPI 3、Actuator
- Testcontainers 集成测试、Docker Compose 一键启动

## 功能概览

### 接口层
- 统一响应 `Result{code,message,data,traceId}`
- 错误码分段：通用 HTTP 码 + 认证 1xxxx / 系统 2xxxx / 文件 3xxxx / 通用 4xxxx
- 全局异常：业务、校验、401/403、404/405/409/413/415、乐观锁、兜底异常
- 参数校验分组、字段级错误明细
- 分页查询 + 排序字段白名单（防止 ORDER BY 注入）
- 基础实体审计字段、逻辑删除、乐观锁
- CORS 白名单、静态上传目录、安全响应头
- TraceId 贯穿响应头、日志和异步任务

### 认证与安全
- 登录 / 注册 / 刷新 / 登出 / 全端登出
- 短期 Access Token + 长期 Refresh Token
- Refresh Token 只存服务端会话，每次刷新轮换并拉黑旧 Access Token
- 令牌家族 + 重放检测：旧刷新令牌再次使用会撤销整个家族
- 并发刷新宽限期（同 IP + UA 幂等返回，默认 10 秒）
- 图形算术验证码（Redis 一次性）
- 登录失败锁定（默认 5 次 / 15 分钟，可通过系统参数调整）
- IP 黑名单、密码策略、改密/重置后强制全端下线
- 在线用户列表、按会话/用户强制下线
- `@Anonymous` 启动时自动收集免登录接口
- `@RateLimit` 注解限流（IP/USER 维度）
- `@RepeatSubmit` 防重复提交（JSON 请求体摘要参与幂等键）

### RBAC 与数据权限
- 用户-角色-菜单/按钮权限，`@PreAuthorize("@ss.hasPermi('...')")`
- ADMIN 角色在权限表达式中直接放行
- 菜单树、动态路由、目录/菜单/按钮三级
- 部门树、`ancestors` 路径维护、移动部门自动更新子孙路径
- 五种数据范围：全部 / 自定义 / 本部门 / 本部门及以下 / 仅本人
- 多角色规则 OR 合并，SQL 全部使用 `#{}` 参数绑定
- 数据权限规则按用户缓存，角色/部门变化时版本化失效

### 系统管理
- 用户：分页、增改删、启停、重置密码、分配角色/岗位、导出/导入/模板
- 角色：分页、增改删、分配菜单、分配自定义部门
- 菜单、部门、岗位、字典、参数 CRUD
- 字典/参数 Redis 缓存，修改即失效；文件大小上限等参数运行时生效
- 操作日志：AOP + 异步线程池 + 敏感字段脱敏 + 参数截断 + 查询/清理/导出
- 登录日志：成功/失败、IP、浏览器、操作系统 + 查询/清理/导出

### 文件与 Excel
- 上传后缀白名单、魔数校验、后缀与内容一致性
- 图片像素上限、OOXML ZIP 条目/解压大小限制
- UUID 文件名、日期目录、路径 normalize 后校验根目录
- 图片静态访问，文档走鉴权下载接口
- 中文文件名 RFC 5987、`X-Content-Type-Options: nosniff`
- Excel 分批游标导出、流式导入监听、字典翻译、模板下载
- 公式注入防护、POI ZIP bomb 防护

## 快速开始

### 方式一：Docker 一键启动

```bash
docker compose up -d --build
```

默认端口：

| 服务 | 地址 | 说明 |
|---|---|---|
| 应用 | http://localhost:8080 | API |
| MySQL | localhost:3306 | campus / campus123 |
| Redis | localhost:6379 | 无密码 |

Compose 使用 `prod` 配置，接口文档默认关闭。首次启动会执行 `src/main/resources/db/schema.sql` 和 `data.sql`。

### 方式二：本地开发

```bash
docker compose up -d mysql redis
mvn spring-boot:run
```

开发配置启用 Knife4j：

- Knife4j：http://localhost:8080/doc.html
- OpenAPI：http://localhost:8080/v3/api-docs
- 健康检查：http://localhost:8080/actuator/health

### 种子账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | ADMIN |

生产环境请立即修改密码，或通过 `sys_user` 初始化脚本替换 BCrypt 哈希。

### 重建数据库

权限模型和表结构已升级，本地已有旧库时需要重建：

```bash
docker compose down -v
docker compose up -d
```

`down -v` 会删除 MySQL 数据卷、上传文件和日志卷，请确认无重要数据。

## 配置说明

### 环境变量（prod）

| 变量 | 说明 | 默认 |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL 连接 | mysql / 3306 / campus |
| `DB_USER` / `DB_PASSWORD` | MySQL 账号 | campus / 无默认 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接 | redis / 6379 / 空 |
| `JWT_SECRET` | JWT 密钥，至少 32 字符 | 无默认，必须提供 |
| `JWT_ACCESS_EXPIRATION` | Access Token 秒数 | 1800 |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 秒数 | 604800 |
| `CAPTCHA_ENABLED` | 是否启用图形验证码 | true |
| `REGISTER_ENABLED` | 是否开放注册 | false |
| `CORS_ALLOWED_ORIGINS` | 允许来源，逗号分隔 | http://localhost:5173 |
| `TRUST_PROXY_HEADERS` | 是否信任 X-Forwarded-For | false |
| `UPLOAD_DIR` | 上传根目录 | /app/uploads |
| `FILE_MAX_SIZE_MB` | 单文件大小上限 | 10 |

`application-dev.yml` 提供本地默认值；`application-test.yml` 关闭验证码和限流，供 Testcontainers 测试使用。

## 接口概览

### 认证与个人中心

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/auth/captcha` | 图形验证码 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/refresh` | 刷新令牌（轮换） |
| POST | `/api/auth/logout` | 登出当前会话 |
| POST | `/api/auth/logout-all` | 退出所有设备 |
| GET/PUT | `/api/users/me` | 当前用户 / 修改资料 |
| PUT | `/api/users/me/password` | 修改密码 |
| POST | `/api/users/me/avatar` | 上传头像 |

### 系统管理

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/system/users` | 用户管理 |
| GET/POST/PUT/DELETE | `/api/system/roles` | 角色管理 |
| PUT | `/api/system/roles/{id}/menus` | 分配菜单 |
| PUT | `/api/system/roles/{id}/depts` | 分配自定义部门 |
| GET/POST/PUT/DELETE | `/api/system/menus` | 菜单管理 |
| GET | `/api/system/menus/routes` | 当前用户动态路由 |
| GET/POST/PUT/DELETE | `/api/system/depts` | 部门管理 |
| GET/POST/PUT/DELETE | `/api/system/posts` | 岗位管理 |
| GET/POST/PUT/DELETE | `/api/system/dict-types` | 字典类型 |
| GET/POST/PUT/DELETE | `/api/system/dict-data` | 字典数据 |
| GET/POST/PUT/DELETE | `/api/system/configs` | 参数配置 |
| GET/DELETE | `/api/system/operation-logs` | 操作日志 |
| GET/DELETE | `/api/system/login-logs` | 登录日志 |
| GET/DELETE | `/api/system/online-users` | 在线用户 / 强制下线 |
| GET/POST/DELETE | `/api/system/ip-blacklist` | IP 黑名单 |

### 文件与 Excel

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/files/upload?scene=IMAGE\|AVATAR\|DOC` | 上传 |
| GET | `/api/files/{id}/download` | 鉴权下载 |
| GET | `/api/system/users/export` | 用户 Excel 导出 |
| POST | `/api/system/users/import` | 用户 Excel 导入 |
| GET | `/api/system/users/import-template` | 导入模板 |
| GET | `/api/system/operation-logs/export` | 操作日志导出 |
| GET | `/api/system/login-logs/export` | 登录日志导出 |

## 项目结构

```text
com.campus
├── auth            # 登录、刷新、验证码、登录安全、令牌会话
├── common
│   ├── annotation  # Anonymous / OperLog / RateLimit / RepeatSubmit / DataScope / Excel
│   ├── aspect      # 限流、防重、数据权限、操作日志
│   ├── cache       # Redis 缓存、键常量、空值占位
│   ├── datascope   # 数据权限规则与缓存
│   ├── excel       # 注解驱动 Excel 导出/导入
│   ├── mybatis     # 审计字段、慢 SQL 拦截器
│   ├── page        # 分页、排序白名单
│   ├── result      # 统一响应、错误码
│   └── web         # TraceId、匿名路径、请求体缓存
├── config          # Redis/MyBatis/CORS/异步/OpenAPI/属性
├── file            # 文件校验、存储、下载
├── security        # JWT、过滤器、LoginUser、权限表达式
└── system          # user/role/menu/dept/post/dict/config/log/online
```

## 关键设计说明

### 双令牌会话

登录时创建 `familyId`，Access 和 Refresh Token 共享 `tokenId`。刷新时：

1. `GETDEL` 原子消费 Refresh Token；
2. 生成同家族新 `tokenId`，旧 Refresh 作废；
3. 拉黑旧 Access Token；
4. 宽限期内同 IP+UA 的重复刷新返回缓存结果；
5. 宽限期外重放则删除整个家族并拉黑 `blacklist_family:{familyId}`。

改密、重置密码、全端下线会增加 `token_version`，旧 Token 在下一次请求时因版本不匹配失效。

### 数据权限

`@DataScope` 注解 + `DataScopeAware` 查询 DTO。服务层使用用户角色生成规则，MyBatis XML 通过 `<choose>/<foreach>` 和 `#{}` 绑定生成条件，不使用 `${}`。ADMIN 直接跳过数据权限。

### 缓存

- Redis 序列化启用类型信息，但使用 `BasicPolymorphicTypeValidator` 只允许 `com.campus.`、`java.util.`、`java.time.`、`java.lang.`，限制反序列化攻击面。
- TTL 增加随机抖动，降低雪崩风险。
- `NullValue` 占位符缓存空结果，降低穿透风险。
- 前缀清理使用 Redis SCAN，不使用 `KEYS`。
- 字典、参数、数据权限、路由、登录用户均有缓存失效策略。

### 上传安全

- 后缀白名单 + 魔数检测 + 图片真实格式/像素检测；
- OOXML 解压后检查条目数量、路径穿越和解压总大小；
- 存储路径 normalize 后校验必须位于上传根目录内；
- 文档不走静态目录，必须通过鉴权下载接口；
- 下载响应强制 `attachment` 与 `nosniff`。

### 审计日志

`@OperLog` 通过 AOP 采集操作人、IP、URL、参数、结果、耗时和异常，提交到有界线程池异步落库。密码、token、secret 等字段在写入前脱敏，参数和结果按长度截断。线程池使用 `CallerRunsPolicy`，宁可轻微阻塞也不丢审计记录。

## 测试

```bash
mvn test
```

- Testcontainers 启动 MySQL 8 和 Redis 7，使用 `db/test-init.sql` 初始化。
- 容器在测试 JVM 内单例启动，避免测试类之间重启导致连接失效。
- 覆盖：401/403、登录注册、令牌轮换与重放、登出、在线用户强制下线、操作/登录日志、分页与校验、数据权限、字典缓存、文件上传/下载、Excel 导出/导入。
- 需要本机 Docker。

仅编译：

```bash
mvn -q -DskipTests package
```

## 暂未实现

- 邮件服务、邮箱验证码、邮箱找回密码
- 公告与已读回执
- 国际化
- 通用定时任务模块（在线会话依赖 Redis TTL 与懒清理）
- 监控大屏、指标采集
- OIDC、对象存储、签名 URL、分布式锁、代码生成器
- 前端页面（本仓库仅提供后端 API 与动态路由数据）

## 安全提示

- 生产环境必须替换 `JWT_SECRET`、数据库密码和种子账号密码。
- 默认不信任 `X-Forwarded-For`；只有在可信反向代理之后才设置 `TRUST_PROXY_HEADERS=true`。
- `CORS_ALLOWED_ORIGINS` 使用精确来源，禁止配置为 `*` 并同时开启凭证。
- 上传目录建议挂载独立卷，不要放在 Web 根目录。
- 导入的 Excel 含初始密码时，导入完成后及时删除源文件。
