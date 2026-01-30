package ps.demo.jpademo.copilot.dto;

import lombok.Data;

@Data
public class AccessTokenRequest {
    private String clientId;
    private String deviceCode;
    private String grantType = "urn:ietf:params:oauth:grant-type:device_code";
}
