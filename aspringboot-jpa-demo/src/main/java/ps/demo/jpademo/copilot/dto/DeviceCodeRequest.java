package ps.demo.jpademo.copilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCodeRequest {
    private String clientId;
    private String scope;
}
