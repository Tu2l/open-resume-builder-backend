package com.tu2l.pdf.utils;

import org.springframework.stereotype.Component;

import com.tu2l.pdf.entities.GeneratedPDFEntity;
import com.tu2l.pdf.models.GenerateAndSavePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;

@Component
public class RequestToEntityMapper {

    public GeneratedPDFEntity map(GeneratePDFResponse response, GenerateAndSavePDFRequest request) {
        GeneratedPDFEntity entity = new GeneratedPDFEntity();
        entity.setFileName(request.getFileName());
        entity.setNumberOfPages(request.getNumberOfPages());
        entity.setUserId(request.getUserId());
        entity.setEncodedPdf(response.getContent());
        return entity;
    }
}
