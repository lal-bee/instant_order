# instant_order 开发规则

这是一个 Spring Boot + Vue3 的连锁餐饮管理毕设项目。

## 当前目标
优先完善以下能力：
1. 统一角色与权限内核
2. 门店/总部数据隔离
3. 套餐总部维度 + 门店套餐上架
4. 统计按角色分域
5. 前后端权限一致性

## 角色定义
- 2：董事长
- 1：店长
- 0：普通员工

禁止混用：
- EMPLOYEE
- MANAGER
- CHAIRMAN

## 开发要求
- 先小改，禁止大面积重构
- 优先保证可运行、可演示、可答辩
- 修改前先列出计划
- 修改后必须输出：
    1. 改了哪些文件
    2. 为什么这样改
    3. 如何启动
    4. 如何测试
    5. 有哪些风险点
- 能复用现有代码就不要另起体系
- 先补后端，再补前端联调
- 涉及 SQL 时，优先新增 migration 脚本，不要直接手改历史脚本
- 每次只完成一个明确功能闭环


# 环境执行规则

## Node / npm 环境说明
- 本机 npm 无法通过 PATH 直接使用
- 必须使用全路径执行

## 命令执行规范（强制）
- 所有 npm 命令必须使用：
  D:\Develop\nvm\nodejs\npm.cmd

- 示例：
  - D:\Develop\nvm\nodejs\npm.cmd install
  - D:\Develop\nvm\nodejs\npm.cmd run dev
  - D:\Develop\nvm\nodejs\npm.cmd run build

## 严禁
- 不允许使用：
  npm
  npx
  pnpm

必须全部替换为全路径 npm.cmd