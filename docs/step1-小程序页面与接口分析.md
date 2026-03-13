# 第 1 步：原微信小程序页面结构与接口调用分析

本文档基于 `hanye-take-out-uniapp` 项目，分析页面结构、路由、接口与需改造点，供后续 Vue 3 H5 迁移使用。

---

## 项目确认与约束（改造前约定）

- **业务形态**：连锁餐饮 **扫码点餐**。用户扫 **餐桌二维码** 打开本站，URL 带 **门店编号、餐桌编号**，用于区分门店并便于服务员按桌号上菜；**用户端不涉及收货地址**（无地址管理、无配送地址选择）。
- **修改范围**：仅修改 `D:\Java_code\hanye-take-out\hanye-take-out-uniapp`，不修改后端及其他模块；涉及其他模块时先询问再执行。
- **命名**：适当修改字段/文案名称，提高与「点餐」项目的关联性（如页面标题、按钮、路由命名等）。
- **配色**（暖色为主 + 中性色承载信息 + 少量高对比强调）：
  - 主色：`#10B981`
  - 背景：`#FFFFFF`
  - 辅助灰：`#E5E7EB`
  - 信息与次要内容用中性色，强调用少量高对比色。

---

## 一、项目技术栈（当前）

- **框架**：UniApp（Vue 3 + TypeScript）
- **编译目标**：微信小程序（`wx.*` / `uni.*` 并存）
- **请求**：`uni.request` 封装在 `@/utils/http.ts`（内部即 wx.request）
- **路由**：`pages.json` 配置 + `uni.navigateTo` / `uni.redirectTo` / `uni.switchTab`
- **状态**：Pinia（user、address、countdown），小程序端用 `uni.getStorageSync` / `uni.setStorageSync` 持久化

---

## 二、页面结构总览

### 2.1 页面列表（与 pages.json 对应）

| 序号 | 路径 | 说明 | 对应 H5 需求 |
|------|------|------|----------------|
| 1 | `pages/index/index` | 首页（轮播 + 点击开始点餐） | ✅ 首页 |
| 2 | `pages/login/login` | 登录页（微信快捷登录） | ✅ 登录页（需替代方案） |
| 3 | `pages/my/my` | 我的（个人信息 + 地址/历史订单/信息设置） | ✅ 我的/订单入口 |
| 4 | `pages/order/order` | 点餐（分类 + 菜品/套餐列表 + 购物车） | ✅ 商品分类+列表+购物车 |
| 5 | `pages/detail/detail` | 菜品/套餐详情 | ✅ 商品详情页 |
| 6 | `pages/submit/submit` | 提交订单（明细、备注、餐具，**无地址**） | ✅ 下单页 |
| 7 | `pages/submit/success` | 提交成功 | 可选（或合并到订单详情） |
| 8 | `pages/address/address` | 地址管理列表 | ❌ 扫码点餐不需要，不实现 |
| 9 | `pages/addOrEditAddress/addOrEditAddress` | 新增/修改地址 | ❌ 不需要，不实现 |
| 10 | `pages/remark/remark` | 备注 | ✅ 备注页 |
| 11 | `pages/pay/pay` | 支付订单（倒计时 + 确认支付） | ✅ 支付确认（不做微信支付） |
| 12 | `pages/orderDetail/orderDetail` | 订单详情 | ✅ 订单详情页 |
| 13 | `pages/history/history` | 历史订单（Tab：全部/待付款/已完成/已取消） | ✅ 订单列表 |
| 14 | `pages/updateMy/updateMy` | 信息设置（头像等） | 可选（H5 可简化） |

### 2.2 TabBar（小程序底部 Tab）

- **首页**：`pages/index/index`
- **我的**：`pages/my/my`  

点餐页 `pages/order/order` 不在 TabBar，通过首页“点击开始点餐”或“我的”内入口进入。

### 2.4 扫码点餐：URL 与上下文

- 用户扫 **餐桌二维码** 进入本站，URL 示例：`https://your-domain.com/?storeId=1&tableId=A12`（或 path 形式，如 `/store/1/table/A12`）。
- **门店编号 storeId、餐桌编号 tableId** 从 URL 解析，整单流程携带（下单、订单详情等），便于区分门店、服务员按桌号上菜。
- 前端不实现地址选择、地址管理；若后端提交订单接口必填 addressId，需与后端协商用固定值或扩展“桌号/门店”字段（涉及后端时先询问再定）。

### 2.5 页面与功能对应关系（扫码点餐 H5 需实现的 6 类）

| 需求 | 小程序页面 | 说明 |
|------|------------|------|
| 首页 | `index` | 轮播 + 按钮跳转点餐 |
| 商品分类+商品列表 | `order` | 左侧分类、右侧菜品/套餐列表，底部购物车 |
| 商品详情页 | `detail` | 菜品详情（含口味规格）或套餐详情 |
| 购物车 | `order` 内 | 底部栏 + 弹层列表，无独立页 |
| 下单页 | `submit` | 订单明细、备注、餐具、去支付（**无地址**；桌号/门店来自 URL） |
| 订单列表 | `history` | 分 Tab 分页列表 |
| 订单详情页 | `orderDetail` | 状态、明细、取消/催单/再来一单 |

---

## 三、接口调用汇总（后端保持不变）

**基础地址**：`http://localhost:8081`（来自 `utils/http.ts`）  
**请求头**：`source-client: miniapp`（H5 可改为 `h5` 或保留，以不影响后端为准）；`Authorization: token`（登录后）。

### 3.1 店铺

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/user/shop/status` | 店铺营业状态（1 营业 0 打烊） |

### 3.2 分类与菜品/套餐

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/user/category/list` | 分类列表（含 type：1 菜品 2 套餐；sort 用于判断） |
| GET | `/user/dish/list/{categoryId}` | 按分类获取菜品列表 |
| GET | `/user/dish/dish/{id}` | 菜品详情（含 flavors） |
| GET | `/user/setmeal/list/{categoryId}` | 按分类获取套餐列表 |
| GET | `/user/setmeal/{id}` | 套餐详情（含 setmealDishes） |

### 3.3 购物车

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/user/cart/add` | 加购，body: `{ dishId?, setmealId?, dishFlavor? }` |
| PUT | `/user/cart/sub` | 减购，body 同上 |
| GET | `/user/cart/list` | 当前购物车列表 |
| DELETE | `/user/cart/clean` | 清空购物车 |

### 3.4 订单

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/user/order/submit` | 提交订单，body 见下方 |
| PUT | `/user/order/payment` | 支付，body: `{ orderNumber, payMethod }` |
| GET | `/user/order/unPayOrderCount` | 未支付订单数量 |
| GET | `/user/order/orderDetail/{id}` | 订单详情 |
| GET | `/user/order/historyOrders` | 历史订单分页，params: `page, pageSize, status?` |
| PUT | `/user/order/cancel/{id}` | 取消订单 |
| POST | `/user/order/reOrder/{id}` | 再来一单 |
| GET | `/user/order/reminder/{id}` | 催单 |

**提交订单 body 示例**（与小程序一致；扫码点餐若后端支持，可传桌号/门店替代或补充 addressId，以与后端确认为准）：

```json
{
  "payMethod": 1,
  "addressId": 1,
  "remark": "",
  "estimatedDeliveryTime": "2025-02-10 12:00:00",
  "deliveryStatus": 1,
  "tablewareNumber": 0,
  "tablewareStatus": 1,
  "packAmount": 2,
  "amount": 38.5
}
```

### 3.5 登录与用户

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/user/user/login` | 登录，body: `{ code: string }`（微信 code） |
| GET | `/user/user/{id}` | 用户信息 |
| PUT | `/user/user` | 更新用户信息 |

### 3.6 地址（扫码点餐 H5 不实现）

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/user/address/default` | 默认地址（H5 不用） |
| GET | `/user/address/list` | 地址列表（H5 不用） |
| … | … | 其他地址接口 H5 均不调用 |

**统一响应结构**（与现有后端一致）：

```json
{ "code": 0, "msg": "ok", "data": { ... } }
```

401 时清除 token 并跳转登录。

---

## 四、导航与路由（需改为 Vue Router）

### 4.1 跳转方式统计

| 方式 | 用途 | 出现位置示例 |
|------|------|--------------|
| `uni.navigateTo` | 保留当前页，打开新页 | index→order, order→submit, detail→submit, my→orderDetail, history→orderDetail |
| `uni.redirectTo` | 关闭当前页再打开 | submit→address/remark, address→submit, remark→submit, pay→success/orderDetail, orderDetail→order, my→address/history/updateMy |
| `uni.switchTab` | 切 Tab | order→index, Navbar 返回→index, login→my, success→index, updateMy→my |

### 4.2 关键跳转一览

- 首页 → 点餐：`/pages/order/order`
- 点餐 → 详情：`/pages/detail/detail?dishId=xx` 或 `setmealId=xx`
- 点餐/详情 → 提交订单：`/pages/submit/submit`
- 提交订单 → 备注：`/pages/remark/remark`（返回 submit 带 remark）  
  （扫码点餐无「选地址」步骤）
- 提交订单 → 支付：`/pages/pay/pay?orderId=xx&orderNumber=xx&orderAmount=xx&orderTime=xx`
- 支付 → 成功：`/pages/submit/success?orderId=...` 或 → 订单详情
- 我的 → 历史订单 / 信息设置（无地址管理）
- 历史订单 / 我的最近订单 → 订单详情：`/pages/orderDetail/orderDetail?orderId=xx`
- 订单详情 → 再来一单：`redirectTo` 点餐页

H5 中：`navigateTo` → `router.push`，`redirectTo` → `router.replace`，`switchTab` → 跳转到 Tab 对应路由（如 `/`、`/my`）。

---

## 五、需移除或替代的微信/小程序能力

### 5.1 必须移除

| 能力 | 位置 | H5 替代 |
|------|------|---------|
| `wx.login()` | `login.vue` onLoad | 不再调用；登录方式见下节 |
| 微信授权（如 getUserProfile） | 当前未使用 | 无需替代 |

### 5.2 登录接口说明（重要）

- 后端 **仅支持**：`POST /user/user/login`，body `{ code }`，后端用 code 调微信 `jscode2session` 拿 openid。
- H5 无法获取微信 code，**接口与参数保持不变**的前提下，有两种常见做法：
  1. **后端增加 H5 登录方式**（如手机号+验证码、或账号密码），您要求不改后端，故暂不采用。
  2. **临时方案**：后端兼容“模拟 code”（如固定 code 或固定 openid）返回固定用户 token，H5 用该 token 存 localStorage，实现“登录状态通过 token 维护”。具体需后端配合一次小改动（仅对 code 做兼容，不改变 URL 和返回结构）。

在未改后端前，前端可先：  
- 登录页提供“模拟登录”按钮，传固定 code（或后端约定的字符串）调现有接口；  
- 若后端暂不支持，可先在前端用 mock token 仅做页面与路由联调，并在注释中说明“正式环境需后端支持 H5 登录或模拟 code”。

### 5.3 需用 Web 能力替代的 API

| 小程序 API | 出现位置 | H5 替代 |
|------------|----------|--------|
| `uni.showToast` | 多处 | 自己封装或轻量 UI（如 Vant）的 Toast |
| `uni.showModal` | orderDetail（取消订单提示） | `confirm()` 或 UI 弹窗 |
| `uni.getSystemInfoSync()` | Navbar、submit、address、addOrEditAddress | 安全区：CSS `env(safe-area-inset-top)`；屏宽等用 `window.innerWidth` 等 |
| `uni.makePhoneCall` | orderDetail、Navbar | `location.href = 'tel:1999'` 或 `<a href="tel:1999">` |
| `uni.chooseMedia` / `wx.getFileSystemManager` | updateMy（头像上传） | `<input type="file">` + FormData 上传（若保留该页） |
| `uni.setNavigationBarTitle` | addOrEditAddress | 用 Vue Router 的 document.title 或路由 meta.title |
| `uni.getStorageSync` / `uni.setStorageSync` | user store 持久化 | `localStorage.getItem` / `setItem` |
| `uni.removeStorage` | addOrEditAddress | `localStorage.removeItem` |

### 5.4 组件与样式

- **scroll-view**：用普通可滚动 `div`（`overflow: auto`）或 `overflow-y: scroll`。
- **swiper**：用 Vue 轮播组件（如 Swiper.js、Vant Swipe）或简单 div 轮播。
- **picker-view**：submit 页餐具份数，用 `<select>` 或自定义下拉/弹层。
- **uni-countdown**：orderDetail、pay 倒计时用 `setInterval` 自实现或通用倒计时组件。
- **rpx**：改为 vw / rem（如 1rpx ≈ 0.5px 或按 375 设计稿换算 vw）。

---

## 六、数据与状态

### 6.1 Pinia Store

- **user**：`profile`（id、openid、token），持久化；H5 用 localStorage。
- **address**：`addressBackUrl`、`defaultCook`（餐具默认选项），可选持久化。
- **countdown**：订单支付倒计时（分钟、秒、timer），不必持久化。

### 6.2 类型定义（可保留逻辑，改为 JS 时去掉类型）

- `types/`：category、dish、setmeal、cart、order、address、user、login 等，与当前接口返回一致；迁移到 JS 时用 JSDoc 或保留为 .d.ts 仅做提示。

---

## 七、与“需要实现的前端页面”的对应关系

| 您列出的页面 | 小程序来源 | 备注 |
|--------------|------------|------|
| 首页 | `index` | 轮播 + 入口到点餐 |
| 商品分类 + 商品列表 | `order` | 左分类右列表，底部购物车 |
| 商品详情页 | `detail` | 菜品/套餐详情，口味规格弹窗 |
| 购物车 | `order` 底部 + 弹层 | 无独立路由 |
| 下单页 | `submit` | 地址、备注、餐具、去支付 |
| 订单列表 | `history` | 分状态 Tab + 分页 |
| 订单详情页 | `orderDetail` | 状态、操作（取消/催单/再来一单） |

不需要：微信支付、微信授权、小程序特有 API（如 wx.login）。

---

## 八、迁移时注意事项小结

1. **业务**：扫码点餐，URL 带 storeId、tableId；整单携带桌号/门店；**不实现地址相关页面与逻辑**。
2. **范围与配色**：仅修改 `hanye-take-out-uniapp`；主色 `#10B981`、背景 `#FFFFFF`、辅助灰 `#E5E7EB`，暖色为主 + 中性色 + 少量高对比强调；命名适当贴近「点餐」。
3. **接口**：URL、请求方法、请求体、响应结构均不改，仅把 `uni.request` 换成 axios；若下单接口必须 addressId，需与后端协商（涉及后端时先询问）。
4. **路由**：`navigateTo` → `push`，`redirectTo` → `replace`，`switchTab` → Tab 路由；备注回传用 query 或 Pinia。
5. **登录**：去掉 wx.login；token 存 localStorage；若后端暂不支持 H5，用“模拟 code”或 mock token 并在注释标明。
6. **UI/能力**：Toast、Modal、电话、安全区、滚动、轮播、倒计时、选择器均用 Web 通用方案实现，并在注释中说明替代关系。
7. **其他模块**：若有修改后端或其它仓库的需求，先询问再执行。

本分析可直接作为第 2 步（Vue H5 目录结构）和第 4 步（逐页迁移）的依据。若你确认无误，可进行第 2 步：设计 Vue H5 项目目录结构。
