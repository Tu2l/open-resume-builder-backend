package com.tu2l.pdf.api.models;

import com.tu2l.pdf.api.models.base.BaseRequest;

import lombok.Data;

/**
 * Request model for resume content.
 */
@Data
public class GenerateResumeRequest implements BaseRequest {
    private String content; // base64 encoded html content
    private String fileName;
    private int numberOfPages;
}
