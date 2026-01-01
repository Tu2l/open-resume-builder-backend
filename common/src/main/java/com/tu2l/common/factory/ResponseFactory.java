package com.tu2l.common.factory;

import com.tu2l.common.model.ErrorResponse;
import com.tu2l.common.model.SuccessResponse;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;

/**
 * Utility class for creating standardized API responses.
 * This class provides static factory methods for common response types.
 */
public final class ResponseFactory {

    private ResponseFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a response instance of the specified class using reflection.
     *
     * @param responseClass The class of the response to create
     * @return A new instance of the response class
     * @throws Exception if instantiation fails
     */
    public static BaseResponse createResponse(Class<? extends BaseResponse> responseClass) throws Exception {
        return responseClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Creates an error response with the specified message.
     *
     * @param message The error message
     * @return An ErrorResponse with FAILURE status
     */
    public static ErrorResponse createErrorResponse(String message) {
        ErrorResponse errorResponse = ErrorResponse.builder().build();
        errorResponse.setMessage(message);
        errorResponse.setStatus(ResponseProcessingStatus.FAILURE);
        return errorResponse;
    }

    /**
     * Creates a success response with the specified message.
     *
     * @param message The success message
     * @return A SuccessResponse with SUCCESS status
     */
    public static SuccessResponse createSuccessResponse(String message) {
        SuccessResponse successResponse = SuccessResponse.builder().build();
        successResponse.setMessage(message);
        successResponse.setStatus(ResponseProcessingStatus.SUCCESS);
        return successResponse;
    }

    /**
     * Sets common response fields (message and status) on a BaseResponse.
     *
     * @param response The response to configure
     * @param message  The message to set
     * @param status   The status to set
     * @param <T>      The type of response
     * @return The configured response for method chaining
     */
    public static <T extends BaseResponse> T configureResponse(T response, String message, ResponseProcessingStatus status) {
        response.setMessage(message);
        response.setStatus(status);
        return response;
    }
}
