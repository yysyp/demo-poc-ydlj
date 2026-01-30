package ps.demo.jpademo.copilot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CopilotTokenResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("expires_at")
    private Long expiresAt;

    @JsonProperty("refresh_in")
    private Integer refreshIn;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("organization_scoped")
    private Boolean organizationScoped;

    @JsonProperty("tracking_id")
    private String trackingId;
}
