package ps.demo.jpademo.copilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ps.demo.jpademo.copilot.config.GitHubCopilotProperties;
import ps.demo.jpademo.copilot.dto.*;
import ps.demo.jpademo.copilot.exception.CopilotAuthException;

import java.time.LocalDateTime;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubDeviceFlowService {

    private final GitHubCopilotProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, LocalDateTime> pendingDeviceCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AccessTokenResponse> tokenCache = new ConcurrentHashMap<>();

    public DeviceFlowInitResponse initiateDeviceFlow() {
        log.info("Initiating GitHub device authorization flow");

        try {
            String scope = "read:user,copilot";
            DeviceCodeRequest request = new DeviceCodeRequest(properties.getClientId(), scope);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", request.getClientId());
            form.add("scope", request.getScope());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            ResponseEntity<DeviceCodeResponse> response = restTemplate.postForEntity(
                    properties.getDeviceCodeUrl(),
                    entity,
                    DeviceCodeResponse.class
            );

            DeviceCodeResponse deviceCodeResponse = response.getBody();
            if (deviceCodeResponse == null) {
                throw new CopilotAuthException("Failed to get device code from GitHub");
            }

            // Store device code with expiration time
            pendingDeviceCodes.put(
                    deviceCodeResponse.getDeviceCode(),
                    LocalDateTime.now().plusSeconds(deviceCodeResponse.getExpiresIn())
            );

            log.info("Device code generated: user_code={}, expires_in={}",
                    deviceCodeResponse.getUserCode(), deviceCodeResponse.getExpiresIn());

            return new DeviceFlowInitResponse(
                    deviceCodeResponse.getUserCode(),
                    deviceCodeResponse.getVerificationUri(),
                    deviceCodeResponse.getDeviceCode(),
                    deviceCodeResponse.getInterval() != null ? deviceCodeResponse.getInterval() : properties.getPollingInterval() / 1000,
                    deviceCodeResponse.getExpiresIn()
            );

        } catch (RestClientException e) {
            log.error("Error initiating device flow", e);
            throw new CopilotAuthException("Failed to initiate device flow", e);
        }
    }

    public AccessTokenResponse pollForToken(String deviceCode) {
        log.info("Polling for access token with device_code={}", deviceCode);

        int attempt = 0;
        int maxAttempts = properties.getMaxPollingAttempts();
        int interval = properties.getPollingInterval();

        while (attempt < maxAttempts) {
            attempt++;
            try {
                // Check if device code is still valid
                LocalDateTime expiration = pendingDeviceCodes.get(deviceCode);
                if (expiration != null && expiration.isBefore(LocalDateTime.now())) {
                    pendingDeviceCodes.remove(deviceCode);
                    throw new CopilotAuthException("DEVICE_CODE_EXPIRED", "Device code has expired");
                }

                // Check if we already have a token
                if (tokenCache.containsKey(deviceCode)) {
                    log.info("Returning cached token for device_code={}", deviceCode);
                    AccessTokenResponse token = tokenCache.get(deviceCode);
                    // Remove from cache after retrieval
                    tokenCache.remove(deviceCode);
                    pendingDeviceCodes.remove(deviceCode);
                    return token;
                }

                // Poll GitHub for token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.set("Accept", "application/json");

                MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                form.add("client_id", properties.getClientId());
                form.add("device_code", deviceCode);
                form.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");

                HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        properties.getAccessTokenUrl(),
                        entity,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());

                    if (jsonNode.has("access_token")) {
                        AccessTokenResponse tokenResponse = objectMapper.treeToValue(jsonNode, AccessTokenResponse.class);
                        tokenCache.put(deviceCode, tokenResponse);
                        pendingDeviceCodes.remove(deviceCode);
                        log.info("Successfully obtained access token for device_code={}", deviceCode);
                        return tokenResponse;
                    } else if (jsonNode.has("error")) {
                        String error = jsonNode.get("error").asText();
                        if ("authorization_pending".equals(error)) {
                            log.debug("Authorization pending for device_code={}, attempt {}/{}",
                                    deviceCode, attempt, maxAttempts);
                            Thread.sleep(interval);
                            continue;
                        } else if ("slow_down".equals(error)) {
                            log.debug("Slow down requested for device_code={}, attempt {}/{}",
                                    deviceCode, attempt, maxAttempts);
                            Thread.sleep(interval * 2);
                            continue;
                        } else {
                            throw new CopilotAuthException(error, jsonNode.get("error_description").asText());
                        }
                    }
                }

                Thread.sleep(interval);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CopilotAuthException("Token polling interrupted", e);
            } catch (Exception e) {
                if (e instanceof CopilotAuthException) {
                    throw (CopilotAuthException) e;
                }
                log.error("Error polling for token", e);
                throw new CopilotAuthException("Error polling for access token", e);
            }
        }

        throw new CopilotAuthException("TIMEOUT", "Token polling timeout after " + maxAttempts + " attempts");
    }

    public DeviceFlowPollResponse pollStatus(String deviceCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", properties.getClientId());
            form.add("device_code", deviceCode);
            form.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getAccessTokenUrl(),
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.has("access_token")) {
                    AccessTokenResponse tokenResponse = objectMapper.treeToValue(jsonNode, AccessTokenResponse.class);
                    tokenCache.put(deviceCode, tokenResponse);
                    pendingDeviceCodes.remove(deviceCode);
                    return new DeviceFlowPollResponse("completed", null, null, tokenResponse);
                } else if (jsonNode.has("error")) {
                    String error = jsonNode.get("error").asText();
                    String errorDesc = jsonNode.has("error_description") ? jsonNode.get("error_description").asText() : null;

                    if ("authorization_pending".equals(error)) {
                        return new DeviceFlowPollResponse("pending", null, null, null);
                    } else if ("slow_down".equals(error)) {
                        return new DeviceFlowPollResponse("slow_down", error, errorDesc, null);
                    } else {
                        pendingDeviceCodes.remove(deviceCode);
                        return new DeviceFlowPollResponse("error", error, errorDesc, null);
                    }
                }
            }

            return new DeviceFlowPollResponse("pending", null, null, null);

        } catch (Exception e) {
            log.error("Error checking device flow status", e);
            return new DeviceFlowPollResponse("error", "polling_error", e.getMessage(), null);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DeviceFlowInitResponse {
        private String userCode;
        private String verificationUri;
        private String deviceCode;
        private Integer interval;
        private Integer expiresIn;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DeviceFlowPollResponse {
        private String status; // pending, completed, error, slow_down
        private String error;
        private String errorDescription;
        private AccessTokenResponse token;
    }
}
