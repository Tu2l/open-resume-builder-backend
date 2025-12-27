package com.tu2l.common.model;

import com.tu2l.common.model.base.BaseResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder()
@Data
public class SuccessResponse extends BaseResponse {

}
