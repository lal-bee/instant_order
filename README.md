# instant_order 项目说明

## 1. 项目简介

`instant_order` 是一个面向连锁餐饮场景的堂食点餐与门店管理系统，包含：

- 管理端（总部/门店后台）
- 用户端（H5 点餐端）
- 后端服务（统一 API 与业务逻辑）

当前已完成核心业务闭环，支持角色权限、门店数据隔离、商品与套餐管理、订单流程、库存与报表等能力。

## 2. 系统结构

项目目录：

- `admin_backend`：Spring Boot 后端（Maven 多模块：`common` / `pojo` / `server`）
- `admin_frontend`：Vue3 + Vite 管理端
- `client_frontend`：Vue3 用户端（Vue CLI）
- `docs`：项目文档与 SQL 脚本

## 3. 核心能力

- 角色权限体系（统一使用数值角色）
  - `2`：董事长
  - `1`：店长
  - `0`：普通员工
- 门店维度数据管理
- 菜品/套餐/分类管理
- 门店菜品库存管理
- 订单与订单明细管理
- 优惠券与用户券
- 餐桌管理与堂食下单
- 工作台与经营统计

## 4. 技术栈

- 后端：Java 17、Spring Boot 3.2.5、MyBatis、Redis、MySQL 8
- 管理端：Vue 3、Vite、TypeScript、Element Plus
- 用户端：Vue 3、Vue Router、Pinia、Axios

## 5. 运行环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Redis 6.x/7.x
- Node.js 20+（用户端 `package.json` 要求 `>=20.20.0`）

后端默认配置（可在 `admin_backend/server/src/main/resources/application.yml` 修改）：

- HTTP 端口：`8081`
- MySQL：`localhost:3306/instant_order`
- Redis：`localhost:6379`

## 6. 数据库初始化

在 MySQL 中创建数据库后执行：

- `docs/instant_order.sql`

示例：

```sql
CREATE DATABASE IF NOT EXISTS instant_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE instant_order;
-- 然后导入 docs/instant_order.sql
```

## 7. 启动步骤

### 7.1 启动后端

```powershell
cd D:\Java_code\instant_order\admin_backend\server
mvn clean compile -DskipTests
mvn spring-boot:run -DskipTests
```

### 7.2 启动管理端（admin_frontend）

```powershell
cd D:\Java_code\instant_order\admin_frontend
D:\Develop\nvm\nodejs\npm.cmd install
D:\Develop\nvm\nodejs\npm.cmd run dev
```

默认访问：`http://localhost:5173`  
代理：`/api` -> `http://localhost:8081/admin`

### 7.3 启动用户端（client_frontend）

```powershell
cd D:\Java_code\instant_order\client_frontend
D:\Develop\nvm\nodejs\npm.cmd install
D:\Develop\nvm\nodejs\npm.cmd run serve
```

默认访问：`http://localhost:5174`  
代理：`/user` -> `http://localhost:8081`

## 8. 构建命令

```powershell
# 后端
cd D:\Java_code\instant_order\admin_backend\server
mvn clean package -DskipTests

# 管理端
cd D:\Java_code\instant_order\admin_frontend
D:\Develop\nvm\nodejs\npm.cmd run build

# 用户端
cd D:\Java_code\instant_order\client_frontend
D:\Develop\nvm\nodejs\npm.cmd run build
```

## 9. 接口分域约定

- 管理端接口：`/admin/*`
- 用户端接口：`/user/*`

典型接口前缀：

- `/admin/employee`、`/admin/store`、`/admin/dish`、`/admin/order`、`/admin/report`
- `/user/user`、`/user/dish`、`/user/cart`、`/user/order`、`/user/table`

## 10. 常见问题

- 管理端无法请求后端：确认后端是否已启动在 `8081`，并检查 Vite 代理配置。
- 用户端跨域：请使用 `npm run serve` 的本地代理，不要直接用静态文件方式打开页面。
- Redis 连接失败：检查 `application.yml` 中 Redis 地址、密码是否和本地一致。
- 数据不一致：优先使用 `docs` 下 SQL 脚本重建库，再执行启动流程。


