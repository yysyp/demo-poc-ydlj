package ps.demo.jpademo.copilot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ps.demo.jpademo.common.BaseResponse;
import ps.demo.jpademo.common.GenericApiResponse;
import ps.demo.jpademo.copilot.dto.AccessTokenResponse;
import ps.demo.jpademo.copilot.service.GitHubDeviceFlowService;

@Slf4j
@RestController
@RequestMapping("/api/auth/github/copilot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeviceFlowController {

    private final GitHubDeviceFlowService deviceFlowService;

    @PostMapping("/device/initiate")
    public ResponseEntity<GenericApiResponse<GitHubDeviceFlowService.DeviceFlowInitResponse>> initiateDeviceFlow() {
        try {
            log.info("Received request to initiate device flow");
            GitHubDeviceFlowService.DeviceFlowInitResponse response = deviceFlowService.initiateDeviceFlow();
            return ResponseEntity.ok(GenericApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error initiating device flow", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("DEVICE_FLOW_INIT_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/device/status")
    public ResponseEntity<GenericApiResponse<GitHubDeviceFlowService.DeviceFlowPollResponse>> checkDeviceStatus(
            @RequestParam String deviceCode) {
        try {
            log.info("Checking device flow status for device_code={}", deviceCode);
            GitHubDeviceFlowService.DeviceFlowPollResponse response = deviceFlowService.pollStatus(deviceCode);

            if ("completed".equals(response.getStatus())) {
                return ResponseEntity.ok(GenericApiResponse.success(response));
            } else if ("error".equals(response.getStatus())) {
                return ResponseEntity.status(400)
                        .body(GenericApiResponse.error(response.getError(), response.getErrorDescription()));
            } else {
                return ResponseEntity.ok(GenericApiResponse.success(response));
            }
        } catch (Exception e) {
            log.error("Error checking device flow status", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("DEVICE_STATUS_CHECK_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/device/token")
    public ResponseEntity<GenericApiResponse<AccessTokenResponse>> getToken(@RequestBody TokenRequest request) {
        try {
            log.info("Requesting token for device_code={}", request.getDeviceCode());
            AccessTokenResponse token = deviceFlowService.pollForToken(request.getDeviceCode());
            return ResponseEntity.ok(GenericApiResponse.success(token));
        } catch (Exception e) {
            log.error("Error getting token", e);
            return ResponseEntity.status(500)
                    .body(GenericApiResponse.error("TOKEN_REQUEST_FAILED", e.getMessage()));
        }
    }

    public static class TokenRequest {
        private String deviceCode;

        public String getDeviceCode() {
            return deviceCode;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }
    }
}
