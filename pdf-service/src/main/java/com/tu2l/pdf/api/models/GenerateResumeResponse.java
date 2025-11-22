package com.tu2l.pdf.api.models;

import com.tu2l.pdf.api.models.base.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GenerateResumeResponse extends BaseResponse  {
    private String content; // base64 encoded html content
    private String fileName;
}
