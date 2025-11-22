package com.tu2l.pdf.models;


import com.tu2l.common.models.base.BaseRequest;

import lombok.Data;

/**
 * Request model for resume content.
 */
@Data
public class GeneratePDFRequest implements BaseRequest {
    private String content; // base64 encoded html content
    private String fileName;
    private int numberOfPages;
}
