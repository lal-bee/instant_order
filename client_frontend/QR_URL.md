# 扫码点餐 - 临时可用 URL

当前系统暂无正式域名，开发/测试可使用以下临时 URL 模拟餐桌二维码：

## 本地开发

```
http://localhost:5174/?storeId=1&tableId=A12
```

- 端口 `5174` 为 H5 前端 devServer 端口
- 确保后端 `http://localhost:8081` 已启动，前端会代理 `/user` 到后端

## 局域网访问（手机扫码测试）

开发机 IP 假设为 `192.168.1.100`，则 URL 为：

```
http://192.168.1.100:5174/?storeId=1&tableId=A12
```

- devServer 已配置 `host: '0.0.0.0'`，可通过局域网 IP 访问
- 手机需与电脑在同一局域网

## 参数说明

| 参数     | 说明       | 示例   |
|----------|------------|--------|
| storeId  | 门店编号   | 1      |
| tableId  | 餐桌/座位号 | A12    |

## 正式发布后

将域名替换为正式域名，例如：

```
https://你的域名.com/?storeId=1&tableId=A12
```

正式环境会将 `storeId`、`tableId` 存入 Redis，下单时从 Redis 取出，无需依赖 URL 传参。
