package com.tu2l.pdf.model;

import com.tu2l.common.model.base.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneratePDFResponse extends BaseResponse {
    private String content; // base64 encoded html content
    private String fileName;
}
