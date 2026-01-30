package ps.demo.jpademo.copilot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeviceCodeResponse {
    @JsonProperty("device_code")
    private String deviceCode;

    @JsonProperty("user_code")
    private String userCode;

    @JsonProperty("verification_uri")
    private String verificationUri;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("interval")
    private Integer interval;
}
