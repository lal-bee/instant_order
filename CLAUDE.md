# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目定位

在线点餐 / 外卖系统，三端拆分：

- `admin_backend`：管理端后端（Spring Boot 3 多模块 Maven 项目）
- `admin_frontend`：管理端前端（Vue 3 + TypeScript + Vite）
- `client_frontend`：用户端前端（Vue CLI，面向小程序/H5）

**修改代码前，必须先确认改的是哪一端。**

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.2.5、MyBatis、Redis、WebSocket |
| 管理端前端 | Vue 3、TypeScript、Vite、Pinia、Element Plus、ECharts、Less |
| 用户端前端 | Vue 3、Vue CLI、Pinia、Axios |
| 接口管理 | Apifox |

**环境要求：** JDK 17+、Node.js 20+、Maven 3.9+

---

## 常用命令

### 后端 (`admin_backend/`)

```bash
# 编译整个多模块项目（在 admin_backend/ 下执行）
mvn clean package -DskipTests

# 运行后端服务（入口：server 模块）
mvn spring-boot:run -pl server

# 运行测试
mvn test -pl server
```

后端服务默认端口：**8081**，数据库：`hanye_take_out`。

### 管理端前端 (`admin_frontend/`)

```bash
npm install
npm run dev        # 开发服务器，端口 5173，自动代理到 localhost:8081
npm run build      # 类型检查 + 构建
npm run lint       # ESLint 修复
npm run format     # Prettier 格式化 src/
```

### 用户端前端 (`client_frontend/`)

```bash
npm install
npm run serve      # 开发服务器（Vue CLI）
npm run build      # 生产构建
```

---

## 后端架构

`admin_backend` 是标准 Maven 多模块项目：

```
admin_backend/
├── common/   # 工具类、常量、异常、JWT、Result 等公共代码
├── pojo/     # DTO / Entity / VO（纯数据类，无业务逻辑）
└── server/   # 业务主模块（Controller → Service → Mapper）
```

**请求路由：**
- `/admin/**` → `controller/admin/`（需要管理员 JWT，`Authorization` header）
- `/user/**`  → `controller/user/`（需要用户 JWT，`authentication` header）
- 登录/注册接口被 `WebMvcConfiguration` 中的拦截器排除在外

**关键横切机制：**
- `AutoFillAspect`：AOP 切面，拦截所有带 `@AutoFill` 注解的 Mapper 方法，自动填充 `create_time / update_time / create_user / update_user` 字段。INSERT 填四个字段，UPDATE 只填两个更新字段，REG（注册）只填时间字段。
- `BaseContext`：ThreadLocal 存储当前请求的用户/员工 ID，供 `AutoFillAspect` 读取。
- `JacksonObjectMapper`：自定义序列化（Long → String 防精度丢失，时间格式统一）。
- `@EnableCaching` + `@EnableScheduling`：启用 Spring 缓存和定时任务。

**配置文件：**
- `application.yml`：公共配置（端口 8081、MySQL、Redis、JWT 密钥）
- `application-dev.yml`：开发环境覆盖（微信支付证书路径等敏感配置）

---

## 管理端前端架构

```
admin_frontend/src/
├── api/          # 按业务模块拆分的 Axios 请求函数
├── components/   # 全局通用组件
├── constants/    # 常量（permission.ts 定义角色枚举和映射）
├── store/        # Pinia store（用户信息，持久化到 localStorage）
├── types/        # TypeScript 类型声明
├── utils/        # request.ts（Axios 实例）、permission.ts、date.ts
├── views/        # 页面组件，按路由模块组织
└── router.ts     # 路由定义 + 权限守卫
```

**代理配置（vite.config.ts）：** 前端 `/api/*` 请求由 Vite 代理转发到 `http://localhost:8081/admin`，path 中 `/api` 前缀会被去掉。

**权限体系（RBAC）：**
- 三个角色：`EMPLOYEE`（普通员工）、`STORE_MANAGER`（店长）、`CHAIRMAN`（董事长）
- 每个路由的 `meta.roles` 控制哪些角色可访问，路由守卫在 `router.ts` 中统一拦截
- 角色别名（包括数字 `0/1/2`）通过 `ROLE_ALIAS_MAP` 统一规范化，新增角色映射时修改 `constants/permission.ts`

**Axios 实例（`utils/request.ts`）：** 请求拦截器自动附加 `Authorization` token；响应拦截器处理业务错误码和 401 跳转登录。

---

## 数据库与 Redis

- MySQL 数据库名：`hanye_take_out`，本地默认账密 `root/123456`
- Redis 默认连接远程服务器（`application.yml` 中配置），本地开发若无访问权限需在 `application-dev.yml` 中覆盖为本地地址
- MyBatis Mapper XML 位于 `server/src/main/resources/mapper/`，数据库迁移 SQL 位于 `server/src/main/resources/db/`

---

## 注意事项

- `application-dev.yml` 中包含微信支付证书路径和 AppSecret 等敏感配置，本地开发需替换为自己的凭证
- 用户端小程序登录依赖微信 `appid` + `secret`，未配置则无法完成登录流程
- 新增 Mapper 方法如需自动填充公共字段，需加 `@AutoFill(OperationType.INSERT/UPDATE)` 注解