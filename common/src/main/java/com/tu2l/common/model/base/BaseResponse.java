package com.tu2l.common.model.base;


import com.tu2l.common.model.states.ResponseProcessingStatus;
import lombok.Data;

/**
 * Base abstract class for all response models.
 */
@Data
public abstract class BaseResponse {
    protected String id = null;
    protected String message = null;
    protected ResponseProcessingStatus status = ResponseProcessingStatus.UNDEFINED;
}
