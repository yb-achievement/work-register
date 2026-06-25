# 个人工作登记 Vue 前端

基于 Vue 3、Vite 和 Element Plus 实现的个人工作登记前端页面。

## 功能

- 展示超时未完成、未完成、已完成工作列表
- 添加新工作弹窗，包含必填校验
- 确认完成操作
- 展示周、月、年度统计卡片
- 生成周工作内容总结弹窗

## 启动

先启动后端服务：

```bash
cd ../work-register-backend
mvn spring-boot:run
```

再启动前端：

```bash
cd ../work-register-frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173/
```

## 后端代理

开发环境中，Vite 会把 `/api` 请求代理到：

```text
http://localhost:8080
```

对应后端接口为：

- `GET /api/work-items`
- `POST /api/work-items`
- `PATCH /api/work-items/{id}/complete`
- `GET /api/work-items/summary?type=week|month|year`
- `GET /api/work-items/weekly-content-summary`
