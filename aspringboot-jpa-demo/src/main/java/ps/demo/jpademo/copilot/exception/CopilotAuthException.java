package ps.demo.jpademo.copilot.exception;

import lombok.Getter;

@Getter
public class CopilotAuthException extends RuntimeException {
    private final String errorCode;

    public CopilotAuthException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public CopilotAuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CopilotAuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }
}
