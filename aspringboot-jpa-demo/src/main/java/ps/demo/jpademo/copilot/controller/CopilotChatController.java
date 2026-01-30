package ps.demo.jpademo.copilot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ps.demo.jpademo.common.GenericApiResponse;
import ps.demo.jpademo.copilot.dto.ChatRequest;
import ps.demo.jpademo.copilot.dto.ChatResponse;
import ps.demo.jpademo.copilot.dto.CopilotTokenResponse;
import ps.demo.jpademo.copilot.service.GitHubCopilotService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/copilot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CopilotChatController {

    private final GitHubCopilotService copilotService;

    @PostMapping("/token")
    public ResponseEntity<GenericApiResponse<CopilotTokenResponse>> getCopilotToken(
            @RequestHeader("Authorization") String authorization) {
        try {
            String githubToken = authorization.replace("Bearer ", "").replace("token ", "");
            log.info("Requesting Copilot token");
            CopilotTokenResponse token = copilotService.getCopilotToken(githubToken);
            return ResponseEntity.ok(GenericApiResponse.success(token));
        } catch (Exception e) {
            log.error("Error getting Copilot token", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("COPILOT_TOKEN_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<GenericApiResponse<ChatResponse>> chat(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChatRequest chatRequest) {
        try {
            String copilotToken = authorization.replace("Bearer ", "");
            log.info("Sending chat request, messages_count={}",
                    chatRequest.getMessages() != null ? chatRequest.getMessages().size() : 0);
            ChatResponse response = copilotService.chat(copilotToken, chatRequest.getMessages());
            return ResponseEntity.ok(GenericApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("CHAT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/chat/simple")
    public ResponseEntity<GenericApiResponse<String>> chatSimple(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChatSimpleRequest request) {
        try {
            String copilotToken = authorization.replace("Bearer ", "");
            log.info("Sending simple chat request");
            String response = copilotService.getChatContent(copilotToken, request.getMessage());
            return ResponseEntity.ok(GenericApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error processing simple chat request", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("CHAT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/chat/conversation")
    public ResponseEntity<GenericApiResponse<String>> chatConversation(
            @RequestHeader("Authorization") String authorization,
            @RequestBody List<ps.demo.jpademo.copilot.dto.Message> messages) {
        try {
            String copilotToken = authorization.replace("Bearer ", "");
            log.info("Sending conversation chat request, messages_count={}", messages.size());
            String response = copilotService.getChatContent(copilotToken, messages);
            return ResponseEntity.ok(GenericApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error processing conversation chat request", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("CHAT_FAILED", e.getMessage()));
        }
    }

    public static class ChatSimpleRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
