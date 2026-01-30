# GitHub Copilot 集成

基于 IntelliJ IDEA GitHub Copilot Chat 的设备授权流程实现的 Java 版本。

## 功能特性

- ✅ GitHub 设备授权流程（Device Authorization Flow）
- ✅ GitHub Copilot Token 获取
- ✅ 调用 GPT-5 模型进行对话
- ✅ 支持多轮对话（保留上下文）
- ✅ 会话管理
- ✅ RESTful API 接口

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        用户应用                                │
│                  (aspringboot-jpa-demo)                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐      ┌──────────────────┐             │
│  │ DeviceFlow       │      │ CopilotChat      │             │
│  │ Controller       │      │ Controller       │             │
│  └────────┬─────────┘      └────────┬─────────┘             │
│           │                          │                       │
│           ▼                          ▼                       │
│  ┌──────────────────┐      ┌──────────────────┐             │
│  │ DeviceFlow       │      │ CopilotChat      │             │
│  │ Service          │      │ Service          │             │
│  └────────┬─────────┘      └────────┬─────────┘             │
│           │                          │                       │
│           ▼                          ▼                       │
│  ┌─────────────────────────────────────────────┐            │
│  │           GitHub API                         │            │
│  │  - /login/device/code                        │            │
│  │  - /login/oauth/access_token                  │            │
│  │  - /copilot_internal/v2/token                 │            │
│  │  - /chat/completions                          │            │
│  └─────────────────────────────────────────────┘            │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 使用流程

### 1. 启动应用

```bash
cd aspringboot-jpa-demo
mvn spring-boot:run
```

### 2. 发起认证

```bash
curl -X POST http://localhost:10001/api/copilot/v1/auth/initiate
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCode": "ABCD-1234",
    "verificationUri": "https://github.com/login/device",
    "deviceCode": "35b4c...",
    "message": "Please visit https://github.com/login/device and enter the code: ABCD-1234"
  }
}
```

### 3. 用户授权

1. 在浏览器中访问 `verificationUri`
2. 输入 `userCode` 进行授权
3. 授权成功后，调用完成认证接口

### 4. 完成认证

```bash
curl -X POST http://localhost:10001/api/copilot/v1/auth/complete \
  -H "Content-Type: application/json" \
  -d '{"deviceCode":"YOUR_DEVICE_CODE"}'
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "copilotToken": "",
    "expiresAt": 1738281600,
    "message": "Authentication successful"
  }
}
```

### 5. 发送消息

```bash
curl -X POST http://localhost:10001/api/copilot/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "YOUR_SESSION_ID",
    "message": "Hello, how are you?"
  }'
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "response": "Hello! I'm doing well, thank you for asking..."
  }
}
```

## API 接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| POST | `/api/copilot/v1/auth/initiate` | 发起设备授权流程 |
| POST | `/api/copilot/v1/auth/complete` | 完成认证（获取 Token） |

### 对话接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| POST | `/api/copilot/v1/chat` | 发送消息 |
| POST | `/api/copilot/v1/chat/stream` | 流式对话 |
| DELETE | `/api/copilot/v1/session/{id}` | 删除会话 |
| GET | `/api/copilot/v1/session/{id}` | 查看会话状态 |

### 底层接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| POST | `/api/auth/github/copilot/device/initiate` | 获取设备码 |
| GET | `/api/auth/github/copilot/device/status` | 检查授权状态 |
| POST | `/api/auth/github/copilot/device/token` | 获取 GitHub Access Token |
| POST | `/api/copilot/token` | 获取 Copilot Token |
| POST | `/api/copilot/chat/simple` | 简单对话 |
| POST | `/api/copilot/chat/conversation` | 多轮对话 |

## 配置说明

在 `application.yml` 中配置：

```yaml
github:
  copilot:
    # GitHub Copilot 官方 Client ID
    client-id: Iv1.b507a08c87ecfe98

    # API 端点
    device-code-url: https://github.com/login/device/code
    access-token-url: https://github.com/login/oauth/access_token
    copilot-token-url: https://api.github.com/copilot_internal/v2/token
    chat-api-url: https://api.githubcopilot.com/chat/completions

    # 模型配置
    model: gpt-5

    # 轮询配置
    polling-interval: 5000  # 轮询间隔（毫秒）
    max-polling-attempts: 60  # 最大轮询次数
```

## 代码结构

```
src/main/java/ps/demo/jpademo/copilot/
├── config/
│   ├── GitHubCopilotProperties.java    # 配置属性
│   └── RestTemplateConfig.java          # HTTP 客户端配置
├── controller/
│   ├── CopilotController.java           # 主控制器（简化版 API）
│   ├── DeviceFlowController.java        # 设备流控制器
│   └── CopilotChatController.java       # 对话控制器
├── service/
│   ├── GitHubDeviceFlowService.java     # 设备授权流程服务
│   └── GitHubCopilotService.java        # Copilot API 服务
├── dto/
│   ├── DeviceCodeRequest.java
│   ├── DeviceCodeResponse.java
│   ├── AccessTokenRequest.java
│   ├── AccessTokenResponse.java
│   ├── CopilotTokenResponse.java
│   ├── Message.java
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── Choice.java
│   └── Usage.java
└── exception/
    ├── CopilotAuthException.java
    └── CopilotApiException.java
```

## 注意事项

1. **Client ID**: 使用 GitHub Copilot 官方 Client ID (`Iv1.b507a08c87ecfe98`)，无需自己创建 OAuth App
2. **设备码有效期**: 默认为 15 分钟，超时后需重新发起授权
3. **Token 有效期**: Copilot Token 有过期时间，过期后需重新获取
4. **会话管理**: 使用内存存储会话信息，应用重启后会话失效

## 测试文件

项目根目录下有 `github-copilot.http` 文件，包含了所有 API 的测试请求示例。

## 依赖

无需额外依赖，使用 Spring Boot 自带的 `RestTemplate` 进行 HTTP 请求。

## 许可证

本项目仅用于学习和演示目的。
