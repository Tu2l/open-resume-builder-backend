package com.tu2l.common.model.base;


import com.tu2l.common.model.states.ResponseProcessingStatus;
import lombok.Data;

/**
 * Base abstract class for all response models.
 */
@Data
public abstract class BaseResponse {
    private String id = null;
    private String message = null;
    private ResponseProcessingStatus status = ResponseProcessingStatus.UNDEFINED;
}
