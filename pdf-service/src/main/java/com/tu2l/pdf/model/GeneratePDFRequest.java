package com.tu2l.pdf.model;


import com.tu2l.common.model.base.BaseRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for resume content.
 */
@Data
public class GeneratePDFRequest implements BaseRequest {
    @NotBlank(message = "BASE64 encoded HTML content cannot be null")
    private String content; // base64 encoded html content
    @NotBlank(message = "File name cannot be null")
    private String fileName;
    @Min(value = 1, message = "Number of pages must be at least 1")
    private int numberOfPages;
}
