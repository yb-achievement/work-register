# 个人工作登记 Spring Boot 后端

这是根据以下文档生成的 Spring Boot 3 后端项目：

- `个人工作登记网页需求说明.md`
- `个人工作登记网页数据库设计.md`

项目使用：

- Java 17
- Spring Boot 3
- Maven
- MyBatis Plus
- MySQL

## 1. 项目结构

```text
work-register-backend
├── pom.xml
├── README.md
└── src
    └── main
        ├── java/com/example/workregister
        │   ├── WorkRegisterApplication.java
        │   ├── common
        │   │   └── Result.java
        │   ├── controller
        │   │   └── WorkItemController.java
        │   ├── dto
        │   ├── entity
        │   ├── exception
        │   ├── mapper
        │   ├── service
        │   └── vo
        └── resources
            ├── application.yml
            └── schema.sql
```

## 2. 数据库准备

先确保本机已经安装并启动 MySQL。

然后执行：

```bash
mysql -u root < src/main/resources/schema.sql
```

如果你的 MySQL 用户名或密码不是 `root/空密码`，请修改：

```text
src/main/resources/application.yml
```

默认配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/work_register
    username: root
    password: ""
```

## 3. 启动项目

进入项目目录：

```bash
cd work-register-backend
```

启动：

```bash
mvn spring-boot:run
```

启动成功后，接口地址默认是：

```text
http://localhost:8080
```

## 4. 返回结构

所有接口统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败时示例：

```json
{
  "code": 400,
  "message": "请选择工作主题",
  "data": null
}
```

## 5. 工作记录接口

### 5.1 新增工作

```http
POST /api/work-items
```

请求示例：

```json
{
  "topic": "数据",
  "content": "整理本周新增合同数据",
  "executor": "张三",
  "dueDate": "2026-06-20"
}
```

### 5.2 查询工作列表

```http
GET /api/work-items
```

支持筛选：

```text
GET /api/work-items?topic=数据
GET /api/work-items?executor=张三
GET /api/work-items?displayStatus=overdue
```

`displayStatus` 可选值：

- `overdue`：超时未完成
- `pending`：未完成
- `done`：已完成

### 5.3 查询详情

```http
GET /api/work-items/{id}
```

### 5.4 修改工作

```http
PUT /api/work-items/{id}
```

请求示例：

```json
{
  "topic": "系统功能",
  "content": "优化工作登记页面的周总结功能",
  "executor": "李四",
  "dueDate": "2026-06-25"
}
```

### 5.5 删除工作

```http
DELETE /api/work-items/{id}
```

当前实现为软删除，会给 `deleted_at` 填入删除时间。

### 5.6 确认完成

```http
PATCH /api/work-items/{id}/complete
```

调用后：

- `status` 变为 `done`
- `completed_at` 记录完成时间

## 6. 统计接口

### 6.1 周/月/年统计

```http
GET /api/work-items/summary?type=week
GET /api/work-items/summary?type=month
GET /api/work-items/summary?type=year
```

返回内容包括：

- 总数
- 已完成数
- 未完成数
- 超时未完成数
- 统计开始日期
- 统计结束日期

周总结范围固定为：

```text
上星期五 至 本星期四
```

### 6.2 生成周总结

```http
GET /api/work-items/weekly-content-summary
```

返回结构：

```json
{
  "completedThisWeek": [
    "数据：整理本周新增合同数据"
  ],
  "nextWeekWork": [
    "系统功能：优化周总结弹窗"
  ],
  "modelType": "rule"
}
```

说明：

- `completedThisWeek` 对应“本周已完成工作”
- `nextWeekWork` 对应“下周工作”
- `modelType = rule` 表示当前是规则生成，不是真实 AI 生成

## 7. curl 测试示例

新增：

```bash
curl -X POST http://localhost:8080/api/work-items \
  -H "Content-Type: application/json" \
  -d '{"topic":"数据","content":"整理本周新增合同数据","executor":"张三","dueDate":"2026-06-20"}'
```

查询列表：

```bash
curl http://localhost:8080/api/work-items
```

确认完成：

```bash
curl -X PATCH http://localhost:8080/api/work-items/1/complete
```

生成周总结：

```bash
curl http://localhost:8080/api/work-items/weekly-content-summary
```

## 8. 目前暂不包含

以下内容先不做：

- 登录
- 多人权限
- 文件上传
- 真实 AI 调用
- 前端页面接入后端
