package ps.demo.jpademo.copilot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    @JsonProperty("usage")
    private Usage usage;
}
