package ps.demo.jpademo.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Generic API Response Wrapper for standardized REST API responses.
 * Used to ensure consistent response structure across all endpoints in financial systems.
 *
 * @param <T> The type of data payload in the response
 */
@Schema(description = "Standardized API response wrapper")
public class GenericApiResponse<T> {

    @Schema(description = "Response status code", example = "2000")
    private String code;

    @Schema(description = "Response message", example = "Success")
    private String message;

    @Schema(description = "Timestamp of response generation")
    private Instant timestamp;

    @Schema(description = "Business data payload")
    private T data;

    @Schema(description = "Request trace ID for observability and debugging")
    private String traceId;

    @Schema(description = "Indicates if the request was successful")
    private boolean success;

    // Private constructor to enforce builder pattern
    private GenericApiResponse() {
        this.timestamp = Instant.now();
    }

    /**
     * Creates a successful response with data
     *
     * @param data The business data
     * @param <T>  Type of data
     * @return GenericApiResponse instance
     */
    public static <T> GenericApiResponse<T> success(T data) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.code = "2000";
        response.message = "Success";
        response.data = data;
        response.success = true;
        return response;
    }

    /**
     * Creates a successful response with custom message
     *
     * @param message Custom success message
     * @param <T>     Type of data
     * @return GenericApiResponse instance
     */
    public static <T> GenericApiResponse<T> success(String message) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.code = "2000";
        response.message = message;
        response.success = true;
        return response;
    }

    /**
     * Creates a successful response with data and custom message
     *
     * @param data    The business data
     * @param message Custom success message
     * @param <T>     Type of data
     * @return GenericApiResponse instance
     */
    public static <T> GenericApiResponse<T> success(T data, String message) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.code = "2000";
        response.message = message;
        response.data = data;
        response.success = true;
        return response;
    }

    /**
     * Creates an error response
     *
     * @param code    Error code
     * @param message Error message
     * @param <T>     Type of data
     * @return GenericApiResponse instance
     */
    public static <T> GenericApiResponse<T> error(String code, String message) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.code = code;
        response.message = message;
        response.success = false;
        return response;
    }

    /**
     * Creates an error response with default 500 code
     *
     * @param message Error message
     * @param <T>     Type of data
     * @return GenericApiResponse instance
     */
    public static <T> GenericApiResponse<T> error(String message) {
        return error("5000", message);
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
