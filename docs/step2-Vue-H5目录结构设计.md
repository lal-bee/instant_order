# 第 2 步：Vue H5 项目目录结构设计

- **位置**：转换后的 Vue 3 H5 项目写入 **`D:\Java_code\hanye-take-out\instant_order_user`**，与 uniapp、后端等并列。
- **技术栈**：Vue 3 + Vue CLI + JavaScript（不用 TypeScript）+ Vue Router + Axios，仅适配移动端。
- **业务**：扫码点餐，URL 带 storeId / tableId，不实现地址相关功能。
- **配色**：暂以功能为主，风格以后再说。

---

## 一、根目录与入口

```
hanye-take-out/
  instant_order_user/      # Vue 3 H5 项目（用户端扫码点餐）
    public/
      index.html           # 入口 HTML 模板
    package.json           # 依赖：vue, vue-router, axios, pinia；构建：@vue/cli-service
    vue.config.js          # Vue CLI 配置，@ -> src，devServer 等
    jsconfig.json          # 可选，IDE 路径提示 @ -> src
    public/                # 静态资源与 index.html 模板
    src/
      main.js              # 入口：createApp、router、pinia、全局样式
      App.vue               # 根组件，<router-view />
```

---

## 二、src 目录结构

### 2.1 全局与样式

| 路径 | 说明 |
|------|------|
| `src/styles/index.css` | 全局样式入口（在 main.js 中引入），可含重置、vw/rem 基准 |
| `src/assets/` | 需打包的图片等（若直接引用上层 static 可用相对路径或复制） |

### 2.2 接口层 api/

所有请求通过 axios 封装发出，**不新建 address 相关接口**。

| 文件 | 说明 |
|------|------|
| `request.js` | axios 实例：baseURL、请求头（如 source-client、Authorization）、响应拦截（401 跳登录、错误提示） |
| `shop.js` | 店铺状态（原 shop.ts） |
| `category.js` | 分类列表（原 category.ts） |
| `dish.js` | 菜品列表、菜品详情（原 dish.ts） |
| `setmeal.js` | 套餐列表、套餐详情（原 setmeal.ts） |
| `cart.js` | 购物车增删改查、清空（原 cart.ts） |
| `order.js` | 下单、支付、订单详情、历史订单、取消、再来一单、催单（原 order.ts） |
| `login.js` | 登录（原 login.ts） |
| `user.js` | 用户信息、更新用户（原 user.ts） |

### 2.3 路由 router/

| 文件 | 说明 |
|------|------|
| `index.js` | 创建 createRouter，定义所有路由；从 query 读取 storeId、tableId，可在路由守卫或工具中注入到全局/store |

**路由与页面对应（无地址相关）**：

| 路由 path | 名称 | 对应页面 | 说明 |
|-----------|------|----------|------|
| `/` | 首页 | home | 轮播 + 进入点餐，进入时带 storeId、tableId |
| `/login` | 登录 | login | 模拟登录，token 存 localStorage |
| `/order` | 点餐 | order | 分类+菜品/套餐列表+购物车，需 storeId、tableId |
| `/dish-detail` | 商品详情 | dish-detail | query: dishId 或 setmealId |
| `/submit` | 提交订单 | submit | 订单明细、备注、餐具、去支付（无地址） |
| `/remark` | 备注 | remark | 填写备注后回 submit |
| `/pay` | 支付确认 | pay | 倒计时、确认支付 |
| `/order-success` | 下单成功 | order-success | 成功页 |
| `/order-list` | 订单列表 | order-list | 历史订单 Tab 分页 |
| `/order-detail` | 订单详情 | order-detail | query: orderId |
| `/my` | 我的 | my | 个人信息、历史订单入口等（无地址管理） |

**Tab 约定**：首页 `/`、我的 `/my` 作为底部 Tab（在 App.vue 或布局组件中判断 path 显示 TabBar）。

### 2.4 状态 stores/

| 文件 | 说明 |
|------|------|
| `user.js` | 用户信息与 token（profile），持久化到 localStorage，替代 uni.getStorageSync |
| `countdown.js` | 支付倒计时（分钟、秒、timer），用于 pay、order-detail |
| （可选）`table.js` | 存当前 storeId、tableId（从 URL 解析），整单携带 |

不实现 address 相关 store（或仅保留极简的“备注/餐具默认”若需）。

### 2.5 工具 utils/

| 文件 | 说明 |
|------|------|
| `url.js` | 从 `window.location.search` 或 route.query 解析 `storeId`、`tableId`，供下单、订单展示使用 |

### 2.6 页面 views/

每个页面一个文件夹，默认入口为 `index.vue`，与路由一一对应；命名与点餐场景关联。

| 目录 | 路由 | 说明 |
|------|------|------|
| `home/` | `/` | 首页 |
| `login/` | `/login` | 登录 |
| `order/` | `/order` | 点餐（分类+列表+购物车） |
| `dish-detail/` | `/dish-detail` | 商品详情 |
| `submit/` | `/submit` | 提交订单 |
| `remark/` | `/remark` | 备注 |
| `pay/` | `/pay` | 支付确认 |
| `order-success/` | `/order-success` | 下单成功 |
| `order-list/` | `/order-list` | 订单列表 |
| `order-detail/` | `/order-detail` | 订单详情 |
| `my/` | `/my` | 我的 |

不包含：address、addOrEditAddress。

### 2.7 组件 components/

| 文件/目录 | 说明 |
|-----------|------|
| `NavBar.vue` | 点餐页顶部栏（对应原 order/components/Navbar.vue），含返回、门店状态等 |
| `Empty.vue` | 空状态（对应原 empty/Empty.vue） |
| 其他 | 按需抽取（如口味选择弹窗、购物车弹层等可放在 order 或公共 components） |

---

## 三、与 uniapp 的对应关系

| uniapp | H5 (h5/src) |
|--------|--------------|
| `pages/index/index` | `views/home/index.vue` |
| `pages/login/login` | `views/login/index.vue` |
| `pages/order/order` | `views/order/index.vue` |
| `pages/order/components/Navbar.vue` | `components/NavBar.vue` |
| `pages/detail/detail` | `views/dish-detail/index.vue` |
| `pages/submit/submit` | `views/submit/index.vue` |
| `pages/remark/remark` | `views/remark/index.vue` |
| `pages/pay/pay` | `views/pay/index.vue` |
| `pages/submit/success` | `views/order-success/index.vue` |
| `pages/history/history` | `views/order-list/index.vue` |
| `pages/orderDetail/orderDetail` | `views/order-detail/index.vue` |
| `pages/my/my` | `views/my/index.vue` |
| `utils/http.ts` | `api/request.js` + 各 api/*.js |
| `stores/modules/user.ts` | `stores/user.js` |
| `stores/modules/countdown.ts` | `stores/countdown.js` |
| 无 | `utils/url.js`（storeId/tableId） |
| `static/` | 可引用 `../src/static` 或复制到 `h5/public` |

---

## 四、静态资源

- 图片/图标：可复用 uniapp 的 `src/static`，或按需复制到 `instant_order_user/public`。
- 字体：若有 iconfont，可复制到 `h5/src/styles` 或 `h5/public`。

---

## 五、小结

- H5 项目独立在 `instant_order_user`，使用 Vue 3 + Vue CLI + JS + Vue Router + Axios + Pinia。
- 路由与页面按上表实现，**不包含地址相关页面与接口**。
- storeId、tableId 由 URL 解析，通过 `utils/url.js` 和（可选）store 在整单流程中传递。
- 下一步（第 3 步）：在 `instant_order_user` 中补全基础 Vue 项目（main.js、router、axios 封装）。
