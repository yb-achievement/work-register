# 自动化部署 CI/CD

## 1. 当前项目结构

| 模块 | 技术栈 | 构建命令 | 构建产物 |
|---|---|---|---|
| 前端 | Vue 3、Vite 7、Element Plus、npm | `npm install && npm run build` | `work-register-frontend/dist/` |
| 后端 | Java 17、Spring Boot 3.3.5、Maven、MyBatis-Plus | `mvn clean package` | `work-register-backend/target/*.jar` |
| 数据库 | MySQL 8 | 不在每次发布时自动初始化 | `work_register` |
| Web 服务 | Nginx | 托管前端并代理 `/api/` | `/var/www/work-register` |
| 后端进程 | systemd | `systemctl restart my-project.service` | `/opt/work-register/app.jar` |

CI/CD 配置文件：

```text
.github/workflows/deploy.yml
```

触发条件：

```text
push 到 main 分支
GitHub Actions 页面手动触发 workflow_dispatch
```

## 2. 流水线执行内容

每次部署依次执行：

1. 拉取 `main` 分支代码。
2. 使用 Node.js 20 执行 `npm install` 和 `npm run build`。
3. 使用 JDK 17 执行 `mvn clean package`。
4. 将前端 `dist` 和后端 jar 打包。
5. 使用 SSH 私钥将发布包上传到 ECS `/tmp`。
6. 将 jar 安装为 `/opt/work-register/app.jar`。
7. 原子替换 `/var/www/work-register` 前端目录。
8. 重启 `my-project.service`。
9. 执行 `nginx -t`，通过后 reload Nginx。
10. 在 ECS 本机和公网分别验证首页与 `/api/work-items`。

生产数据库配置继续保留在 ECS：

```text
/opt/work-register/config/application-prod.yml
```

流水线不会上传或覆盖该文件，也不会把数据库密码保存到 Git。

## 3. 初始化 Git 仓库

当前 `/Users/yebin/Desktop/codex_workregister` 尚未发现 `.git`，建议将前后端作为一个仓库管理：

```bash
cd /Users/yebin/Desktop/codex_workregister
git init
git branch -M main
git add .
git commit -m "Configure ECS CI/CD"
git remote add origin git@github.com:你的账号/你的仓库.git
git push -u origin main
```

如果远端仓库已经存在，先确认其中没有需要保留但本机缺少的提交，再决定合并方式，不要直接强制推送。

## 4. 创建部署专用 SSH 密钥

在本机创建一对专用于 GitHub Actions 的密钥，不要使用个人日常 SSH 私钥：

```bash
ssh-keygen -t ed25519 -C "github-actions-work-register" \
  -f ~/.ssh/work_register_deploy
```

将公钥安装到 ECS。首次执行需要输入服务器密码：

```bash
ssh-copy-id -i ~/.ssh/work_register_deploy.pub root@47.99.141.210
```

测试私钥登录：

```bash
ssh -i ~/.ssh/work_register_deploy root@47.99.141.210
```

建议后续创建非 root 部署用户。若使用非 root 用户，该用户必须能够免密码执行以下命令：

```text
install、cp、chown、mv、rm
systemctl restart my-project.service
systemctl reload nginx
systemctl status my-project.service
nginx -t
```

## 5. 配置 GitHub Secrets

进入 GitHub 仓库：

```text
Settings -> Secrets and variables -> Actions -> New repository secret
```

添加以下 Secrets：

| Secret | 是否必填 | 内容 |
|---|---|---|
| `ECS_HOST` | 是 | ECS 公网 IP，例如 `47.99.141.210` |
| `ECS_USER` | 是 | SSH 用户，当前可填写 `root` |
| `ECS_SSH_PORT` | 否 | SSH 端口；不配置时使用 `22` |
| `ECS_SSH_PRIVATE_KEY` | 是 | `~/.ssh/work_register_deploy` 私钥的完整内容 |
| `ECS_KNOWN_HOSTS` | 是 | ECS SSH 主机公钥记录 |
| `APP_BASE_URL` | 否 | 公网访问地址，例如 `http://47.99.141.210`；配置 HTTPS 后填写 HTTPS 地址 |

读取私钥内容：

```bash
cat ~/.ssh/work_register_deploy
```

生成并核对 `ECS_KNOWN_HOSTS`：

```bash
ssh-keyscan -H 47.99.141.210
```

首次采集主机公钥时，应与 ECS 控制台或已可信连接中的主机指纹核对，然后将完整输出保存到 `ECS_KNOWN_HOSTS`。

不要把以下内容提交到仓库：

```text
服务器密码
数据库密码
SSH 私钥
application-prod.yml
.env.local
```

## 6. ECS 前置条件

流水线假设服务器已经存在：

```text
/opt/work-register/config/application-prod.yml
/etc/systemd/system/my-project.service
/etc/nginx/sites-enabled/work-register
```

检查 systemd 服务使用的 jar 路径：

```bash
sudo systemctl cat my-project.service
```

应包含：

```ini
ExecStart=/usr/bin/java -jar /opt/work-register/app.jar \
  --spring.config.location=file:/opt/work-register/config/application-prod.yml
```

若当前配置不是此路径，修改服务：

```bash
sudo systemctl edit --full my-project.service
sudo systemctl daemon-reload
sudo systemctl restart my-project.service
```

检查 Nginx：

```bash
sudo nginx -t
sudo systemctl is-enabled nginx
sudo systemctl is-active nginx
```

Nginx 前端根目录和 API 代理应为：

```nginx
root /var/www/work-register;

location / {
    try_files $uri $uri/ /index.html;
}

location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 7. 首次运行和验证

提交并推送：

```bash
git add .github/workflows/deploy.yml .gitignore DEPLOY_CICD.md
git commit -m "Add automated ECS deployment"
git push origin main
```

在 GitHub 仓库的 `Actions -> Build and Deploy` 查看执行日志。

部署后手动复核：

```bash
curl -fsS http://47.99.141.210/
curl -fsS http://47.99.141.210/api/work-items
ssh root@47.99.141.210 \
  "systemctl is-active my-project.service && systemctl is-active nginx"
```

业务接口预期包含：

```json
{"code":0,"message":"success","data":[]}
```

## 8. 回滚方式

流水线部署过程中会临时保留上一版前端目录：

```text
/var/www/work-register.old
```

只有新版本后端、Nginx和接口验证全部通过后才删除。若发布中途失败，可登录 ECS 检查并恢复。

后端建议在正式使用前增加版本化备份。手动回滚示例：

```bash
sudo cp /opt/work-register/backups/app-上一版本.jar /opt/work-register/app.jar
sudo systemctl restart my-project.service
curl -fsS http://127.0.0.1:8080/api/work-items
```

## 9. Gitee 和 GitLab 对应方案

当前目录未初始化 Git，因此无法从 `git remote -v` 判断最终托管平台。本项目已经生成 GitHub Actions 方案。

### Gitee

Gitee Go 可将 `.github/workflows/deploy.yml` 的构建与 SSH 命令迁移到 `.workflow/` 流水线；另一种常见方式是在 ECS 使用 WebHook 触发 Jenkins。敏感信息应保存为 Gitee 流水线环境变量，不写入 YAML。

核心阶段保持一致：

```text
Node.js 20 构建前端
JDK 17 + Maven 构建后端
SSH/SCP 上传发布包
远程 systemctl restart
nginx -t && systemctl reload nginx
curl 健康检查
```

### GitLab

GitLab 使用仓库根目录 `.gitlab-ci.yml`。将以下变量配置为 `Settings -> CI/CD -> Variables` 中的 Masked、Protected 变量：

```text
ECS_HOST
ECS_USER
ECS_SSH_PORT
ECS_SSH_PRIVATE_KEY
ECS_KNOWN_HOSTS
APP_BASE_URL
```

GitLab Runner 中同样执行本工作流的构建、`scp`、`ssh` 和验收命令即可。生产部署 job 应限制为：

```yaml
rules:
  - if: '$CI_COMMIT_BRANCH == "main"'
```

无论使用 GitHub、Gitee 还是 GitLab，都不应使用服务器登录密码进行自动部署；应使用可撤销的部署专用 SSH 密钥。
