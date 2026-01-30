package ps.demo.jpademo.copilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ps.demo.jpademo.copilot.config.GitHubCopilotProperties;
import ps.demo.jpademo.copilot.dto.*;
import ps.demo.jpademo.copilot.exception.CopilotApiException;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubCopilotService {

    private final GitHubCopilotProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CopilotTokenResponse getCopilotToken(String githubToken) {
        log.info("Requesting Copilot token from GitHub");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + githubToken);
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CopilotTokenResponse> response = restTemplate.exchange(
                    properties.getCopilotTokenUrl(),
                    HttpMethod.GET,
                    entity,
                    CopilotTokenResponse.class
            );

            CopilotTokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || tokenResponse.getToken() == null) {
                throw new CopilotApiException("Failed to get Copilot token from GitHub");
            }

            log.info("Successfully obtained Copilot token, expires_at={}", tokenResponse.getExpiresAt());
            return tokenResponse;

        } catch (RestClientException e) {
            log.error("Error getting Copilot token", e);
            throw new CopilotApiException("Failed to get Copilot token", e);
        }
    }

    public ChatResponse chat(String copilotToken, String message) {
        return chat(copilotToken, Collections.singletonList(new Message("user", message)));
    }

    public ChatResponse chat(String copilotToken, java.util.List<Message> messages) {
        log.info("Sending chat request to Copilot API, message_count={}", messages.size());

        try {
            ChatRequest request = new ChatRequest();
            request.setModel(properties.getModel());
            request.setMessages(messages);
            request.setStream(false);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + copilotToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getChatApiUrl(),
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                // Handle both OpenAI-style and Copilot-style responses
                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                        jsonNode.get("choices").size() > 0) {
                    ChatResponse chatResponse = objectMapper.treeToValue(jsonNode, ChatResponse.class);
                    log.info("Received chat response, choices={}", chatResponse.getChoices().size());
                    return chatResponse;
                } else if (jsonNode.has("message")) {
                    // Handle alternative response format
                    String content = jsonNode.get("message").asText();
                    Choice choice = new Choice();
                    choice.setMessage(new Message("assistant", content));
                    ChatResponse chatResponse = new ChatResponse();
                    chatResponse.setChoices(Collections.singletonList(choice));
                    return chatResponse;
                } else {
                    throw new CopilotApiException("Unexpected response format from Copilot API");
                }
            } else {
                throw new CopilotApiException("Failed to get response from Copilot API");
            }

        } catch (RestClientException e) {
            log.error("Error calling Copilot chat API", e);
            throw new CopilotApiException("Failed to call Copilot chat API", e);
        } catch (Exception e) {
            if (e instanceof CopilotApiException) {
                throw (CopilotApiException) e;
            }
            log.error("Error processing chat response", e);
            throw new CopilotApiException("Error processing chat response", e);
        }
    }

    public String getChatContent(String copilotToken, String message) {
        ChatResponse response = chat(copilotToken, message);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        throw new CopilotApiException("No response content from Copilot");
    }

    public String getChatContent(String copilotToken, java.util.List<Message> messages) {
        ChatResponse response = chat(copilotToken, messages);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        throw new CopilotApiException("No response content from Copilot");
    }
}
