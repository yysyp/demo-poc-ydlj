package ps.demo.jpademo.copilot;

import ps.demo.jpademo.copilot.dto.ChatRequest;
import ps.demo.jpademo.copilot.dto.CopilotTokenResponse;
import ps.demo.jpademo.copilot.dto.Message;
import ps.demo.jpademo.copilot.service.GitHubCopilotService;
import ps.demo.jpademo.copilot.service.GitHubDeviceFlowService;

/**
 * GitHub Copilot 集成演示
 *
 * 使用方法：
 * 1. 启动应用
 * 2. 调用设备授权流程 API
 * 3. 用户在浏览器中授权
 * 4. 获取 Token 后调用对话 API
 */
public class GitHubCopilotDemo {

    /**
     * 示例代码：通过设备授权流程认证并调用 Copilot
     *
     * 1. 调用 POST /api/copilot/v1/auth/initiate 获取设备码
     * 2. 用户访问 verificationUri 并输入 userCode
     * 3. 调用 POST /api/copilot/v1/auth/complete 完成认证
     * 4. 调用 POST /api/copilot/v1/chat 发送消息
     */

    /*
     * REST API 使用示例
     */

    // 1. 发起认证
    /*
    POST http://localhost:10001/api/copilot/v1/auth/initiate
    Content-Type: application/json

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "userCode": "ABCD-1234",
            "verificationUri": "https://github.com/login/device",
            "deviceCode": "...",
            "message": "Please visit https://github.com/login/device and enter the code: ABCD-1234"
        }
    }
    */

    // 2. 完成认证（用户授权后调用）
    /*
    POST http://localhost:10001/api/copilot/v1/auth/complete
    Content-Type: application/json

    {
        "deviceCode": "device_code_from_step_1"
    }

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "sessionId": "uuid",
            "copilotToken": "...",
            "expiresAt": 1234567890,
            "message": "Authentication successful"
        }
    }
    */

    // 3. 发送消息
    /*
    POST http://localhost:10001/api/copilot/v1/chat
    Content-Type: application/json

    {
        "sessionId": "session_id_from_step_2",
        "message": "Hello, how are you?"
    }

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "response": "Hello! I'm doing well, thank you for asking..."
        }
    }
    */

    /**
     * Java 代码直接调用示例
     */
    public static void main(String[] args) {
        // 1. 发起设备授权流程
        // GitHubDeviceFlowService.DeviceFlowInitResponse deviceFlow =
        //         deviceFlowService.initiateDeviceFlow();
        // System.out.println("Please visit: " + deviceFlow.getVerificationUri());
        // System.out.println("Enter code: " + deviceFlow.getUserCode());

        // 2. 等待用户授权，然后获取 Token
        // AccessTokenResponse token = deviceFlowService.pollForToken(deviceFlow.getDeviceCode());

        // 3. 获取 Copilot Token
        // CopilotTokenResponse copilotToken = copilotService.getCopilotToken(token.getAccessToken());

        // 4. 调用 Copilot Chat
        // String response = copilotService.getChatContent(copilotToken.getToken(), "Hello!");
        // System.out.println("Response: " + response);

        // 5. 多轮对话
        // List<Message> messages = new ArrayList<>();
        // messages.add(new Message("user", "What is Java?"));
        // ChatResponse response1 = copilotService.chat(copilotToken.getToken(), messages);
        //
        // messages.add(new Message("assistant", response1.getChoices().get(0).getMessage().getContent()));
        // messages.add(new Message("user", "Can you give me an example?"));
        // ChatResponse response2 = copilotService.chat(copilotToken.getToken(), messages);
        //
        // System.out.println("Response 2: " + response2.getChoices().get(0).getMessage().getContent());
    }

    /**
     * 流式对话示例（保留上下文）
     */
    public void conversationalChatExample(String copilotToken) {
        /*
        List<Message> conversationHistory = new ArrayList<>();

        // 第一轮对话
        conversationHistory.add(new Message("user", "Please explain what is Spring Boot"));
        String response1 = copilotService.getChatContent(copilotToken, conversationHistory);
        conversationHistory.add(new Message("assistant", response1));

        // 第二轮对话（会记住之前的对话）
        conversationHistory.add(new Message("user", "How do I create a simple REST controller?"));
        String response2 = copilotService.getChatContent(copilotToken, conversationHistory);
        conversationHistory.add(new Message("assistant", response2));

        // 第三轮对话
        conversationHistory.add(new Message("user", "Show me an example of dependency injection"));
        String response3 = copilotService.getChatContent(copilotToken, conversationHistory);
        System.out.println("Response 3: " + response3);
        */
    }
}
