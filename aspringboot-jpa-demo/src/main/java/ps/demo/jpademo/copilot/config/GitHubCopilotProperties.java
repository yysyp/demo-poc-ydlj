package ps.demo.jpademo.copilot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.copilot")
public class GitHubCopilotProperties {

    private String clientId = "Iv1.b507a08c87ecfe98";
    private String deviceCodeUrl = "https://github.com/login/device/code";
    private String accessTokenUrl = "https://github.com/login/oauth/access_token";
    private String copilotTokenUrl = "https://api.github.com/copilot_internal/v2/token";
    private String chatApiUrl = "https://api.githubcopilot.com/chat/completions";
    private String model = "gpt-5";
    private int pollingInterval = 5000;
    private int maxPollingAttempts = 60;
}
