package com.tu2l.pdf.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateAndSavePDFRequest extends GeneratePDFRequest {
    @NotBlank(message = "User ID cannot be empty")
    private String userId;
}
