package ps.demo.jpademo.copilot.exception;

import lombok.Getter;

@Getter
public class CopilotApiException extends RuntimeException {
    private final String errorCode;

    public CopilotApiException(String message) {
        super(message);
        this.errorCode = "API_ERROR";
    }

    public CopilotApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CopilotApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "API_ERROR";
    }
}
