package ps.demo.jpademo.copilot.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Boolean stream = false;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
}
