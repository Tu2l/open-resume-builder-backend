package com.tu2l.pdf.models;

import com.tu2l.common.models.base.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneratePDFResponse extends BaseResponse {
    private String content; // base64 encoded html content
    private String fileName;
}
