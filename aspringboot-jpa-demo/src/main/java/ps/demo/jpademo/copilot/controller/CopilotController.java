package ps.demo.jpademo.copilot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ps.demo.jpademo.common.GenericApiResponse;
import ps.demo.jpademo.common.GenericApiResponse;
import ps.demo.jpademo.copilot.dto.*;
import ps.demo.jpademo.copilot.service.GitHubCopilotService;
import ps.demo.jpademo.copilot.service.GitHubDeviceFlowService;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/copilot/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CopilotController {

    private final GitHubDeviceFlowService deviceFlowService;
    private final GitHubCopilotService copilotService;

    private final ConcurrentHashMap<String, SessionState> activeSessions = new ConcurrentHashMap<>();

    @PostMapping("/auth/initiate")
    public ResponseEntity<GenericApiResponse<AuthInitResponse>> initiateAuth() {
        try {
            log.info("Initiating GitHub Copilot authentication");
            GitHubDeviceFlowService.DeviceFlowInitResponse deviceFlow =
                    deviceFlowService.initiateDeviceFlow();

            AuthInitResponse response = new AuthInitResponse(
                    deviceFlow.getUserCode(),
                    deviceFlow.getVerificationUri(),
                    deviceFlow.getDeviceCode(),
                    "Please visit " + deviceFlow.getVerificationUri() +
                            " and enter the code: " + deviceFlow.getUserCode()
            );

            return ResponseEntity.ok(GenericApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error initiating authentication", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("AUTH_INIT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/auth/complete")
    public ResponseEntity<GenericApiResponse<AuthCompleteResponse>> completeAuth(
            @RequestBody AuthCompleteRequest request) {
        try {
            log.info("Completing authentication for device_code={}", request.getDeviceCode());

            // Poll for token in background
            CompletableFuture<AuthCompleteResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    AccessTokenResponse githubToken = deviceFlowService.pollForToken(request.getDeviceCode());
                    CopilotTokenResponse copilotToken = copilotService.getCopilotToken(githubToken.getAccessToken());

                    String sessionId = java.util.UUID.randomUUID().toString();
                    SessionState session = new SessionState(
                            sessionId,
                            githubToken.getAccessToken(),
                            copilotToken.getToken(),
                            copilotToken.getExpiresAt()
                    );
                    activeSessions.put(sessionId, session);

                    return new AuthCompleteResponse(
                            sessionId,
                            copilotToken.getToken(),
                            copilotToken.getExpiresAt(),
                            "Authentication successful"
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            AuthCompleteResponse response = future.get(120, java.util.concurrent.TimeUnit.SECONDS);
            return ResponseEntity.ok(GenericApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error completing authentication", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("AUTH_COMPLETE_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<GenericApiResponse<ChatMessageResponse>> chat(
            @RequestBody ChatUserRequest request) {
        try {
            if (request.getSessionId() == null || !activeSessions.containsKey(request.getSessionId())) {
                return ResponseEntity.status(401)
                        .body(GenericApiResponse.error("INVALID_SESSION", "Please authenticate first"));
            }

            SessionState session = activeSessions.get(request.getSessionId());

            log.info("Processing chat request for session={}", request.getSessionId());
            String response = copilotService.getChatContent(
                    session.getCopilotToken(),
                    request.getMessage()
            );

            return ResponseEntity.ok(GenericApiResponse.success(new ChatMessageResponse(response)));

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("CHAT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/chat/stream")
    public ResponseEntity<GenericApiResponse<String>> chatStream(
            @RequestBody ChatUserRequest request) {
        try {
            if (request.getSessionId() == null || !activeSessions.containsKey(request.getSessionId())) {
                return ResponseEntity.status(401)
                        .body(GenericApiResponse.error("INVALID_SESSION", "Please authenticate first"));
            }

            SessionState session = activeSessions.get(request.getSessionId());

            log.info("Processing stream chat request for session={}", request.getSessionId());
            ChatResponse response = copilotService.chat(
                    session.getCopilotToken(),
                    Collections.singletonList(new Message("user", request.getMessage()))
            );

            String content = response.getChoices() != null && !response.getChoices().isEmpty()
                    ? response.getChoices().get(0).getMessage().getContent()
                    : "No response";

            return ResponseEntity.ok(GenericApiResponse.success(content));

        } catch (Exception e) {
            log.error("Error processing stream chat request", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("CHAT_STREAM_FAILED", e.getMessage()));
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<GenericApiResponse<String>> logout(@PathVariable String sessionId) {
        try {
            activeSessions.remove(sessionId);
            log.info("Session {} logged out", sessionId);
            return ResponseEntity.ok(GenericApiResponse.success("Logged out successfully"));
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("LOGOUT_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<GenericApiResponse<SessionInfo>> getSessionInfo(@PathVariable String sessionId) {
        try {
            SessionState session = activeSessions.get(sessionId);
            if (session == null) {
                return ResponseEntity.status(404)
                        .body(GenericApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }

            SessionInfo info = new SessionInfo(
                    session.getSessionId(),
                    session.getExpiresAt() != null
                            && session.getExpiresAt() > System.currentTimeMillis() / 1000
            );

            return ResponseEntity.ok(GenericApiResponse.success(info));
        } catch (Exception e) {
            log.error("Error getting session info", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("SESSION_INFO_FAILED", e.getMessage()));
        }
    }

    // DTOs
    public static class AuthInitResponse {
        private String userCode;
        private String verificationUri;
        private String deviceCode;
        private String message;

        public AuthInitResponse(String userCode, String verificationUri, String deviceCode, String message) {
            this.userCode = userCode;
            this.verificationUri = verificationUri;
            this.deviceCode = deviceCode;
            this.message = message;
        }

        public String getUserCode() { return userCode; }
        public String getVerificationUri() { return verificationUri; }
        public String getDeviceCode() { return deviceCode; }
        public String getMessage() { return message; }
    }

    public static class AuthCompleteRequest {
        private String deviceCode;

        public String getDeviceCode() { return deviceCode; }
        public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    }

    public static class AuthCompleteResponse {
        private String sessionId;
        private String copilotToken;
        private Long expiresAt;
        private String message;

        public AuthCompleteResponse(String sessionId, String copilotToken, Long expiresAt, String message) {
            this.sessionId = sessionId;
            this.copilotToken = copilotToken;
            this.expiresAt = expiresAt;
            this.message = message;
        }

        public String getSessionId() { return sessionId; }
        public String getCopilotToken() { return copilotToken; }
        public Long getExpiresAt() { return expiresAt; }
        public String getMessage() { return message; }
    }

    public static class ChatUserRequest {
        private String sessionId;
        private String message;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatMessageResponse {
        private String response;

        public ChatMessageResponse(String response) {
            this.response = response;
        }

        public String getResponse() { return response; }
    }

    public static class SessionInfo {
        private String sessionId;
        private boolean isValid;

        public SessionInfo(String sessionId, boolean isValid) {
            this.sessionId = sessionId;
            this.isValid = isValid;
        }

        public String getSessionId() { return sessionId; }
        public boolean isValid() { return isValid; }
    }

    private static class SessionState {
        private String sessionId;
        private String githubToken;
        private String copilotToken;
        private Long expiresAt;

        public SessionState(String sessionId, String githubToken, String copilotToken, Long expiresAt) {
            this.sessionId = sessionId;
            this.githubToken = githubToken;
            this.copilotToken = copilotToken;
            this.expiresAt = expiresAt;
        }

        public String getSessionId() { return sessionId; }
        public String getGithubToken() { return githubToken; }
        public String getCopilotToken() { return copilotToken; }
        public Long getExpiresAt() { return expiresAt; }
    }
}
