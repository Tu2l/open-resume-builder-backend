package com.tu2l.common.factory;

import com.tu2l.common.model.ErrorResponse;
import com.tu2l.common.model.SuccessResponse;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;

public class ResponseFactory {
    public static BaseResponse createResponse(Class<? extends BaseResponse> responseClass) throws Exception {
        return responseClass.getDeclaredConstructor().newInstance();
    }

    public static ErrorResponse createErrorResponse(String message, String id, ResponseProcessingStatus status) {
        ErrorResponse errorResponse = ErrorResponse.builder().build();
        errorResponse.setMessage(message);
        errorResponse.setId(id);
        errorResponse.setStatus(status);
        return errorResponse;
    }

    public static SuccessResponse createSuccessResponse(String message) {
        SuccessResponse successResponse = SuccessResponse.builder().build();
        successResponse.setMessage(message);
        successResponse.setStatus(ResponseProcessingStatus.SUCCESS);
        return successResponse;
    }
}
