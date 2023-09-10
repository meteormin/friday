package com.miniyus.friday.common.error;

import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 * 
 * common error codes
 *
 * @author miniyus
 * @date 2023/09/01
 */
public enum ErrorCode {
    BAD_REQUEST(10, 400), // bad request
    UNAUTHORIZED(11, 401), // unauthorized
    FORBIDDEN(12, 403), // forbidden
    NOT_FOUND(13, 404), // not found
    METHOD_NOT_ALLOWED(14, 405), // method not allowed
    CONFLICT(15, 409), // conflict
    TOO_MANY_REQUESTS(16, 429), // too many requests
    INTERNAL_SERVER_ERROR(20, 500), // internal server error
    SERVER_UNAVAILABLE(21, 503); // server unavailable

    private final int errorCode;
    private final int statusCode;

    ErrorCode(int code, int status) {
        this.errorCode = code;
        this.statusCode = status;
    }

    /**
     * Retrieves the code value.
     *
     * @return the code value
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * A description of the entire Java function.
     *
     * @param None No parameters are accepted by this function.
     * @return int The status value of the function.
     */
    public int getStatusCode() {
        return statusCode;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.valueOf(statusCode);
    }

    /**
     * A function to retrieve an ErrorCode object based on a given code.
     *
     * @param code the code to match against the ErrorCode objects
     * @return the ErrorCode object matching the given code, or null if no match is found
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getErrorCode() == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * Finds and returns the ErrorCode object that corresponds to the given status.
     *
     * @param status the status to search for
     * @return the ErrorCode object that matches the given status, or null if no match is found
     */
    public static ErrorCode fromStatus(int status) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getStatusCode() == status) {
                return e;
            }
        }
        return null;
    }
}
