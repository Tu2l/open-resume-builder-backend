package com.tu2l.pdf.util;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.pdf.entity.GeneratedPDFEntity;
import com.tu2l.pdf.model.request.GenerateAndSavePDFRequest;
import com.tu2l.pdf.model.response.GeneratePDFResponse;

@Component
public class EntityMapper {
    private static final String PLACEHOLDER_CONTENT = "PDF content placeholder";

    public Optional<GeneratedPDFEntity> map(GeneratePDFResponse response, GenerateAndSavePDFRequest request) {
        if (response == null || request == null) {
            return Optional.empty();
        }

        GeneratedPDFEntity entity = new GeneratedPDFEntity();
        entity.setFileName(request.getFileName());
        entity.setNumberOfPages(request.getNumberOfPages());
        entity.setUserId(request.getUserId());
        entity.setEncodedPdf(response.getContent());
        return Optional.of(entity);
    }

      public Optional<GeneratedPDFEntity> map(GenerateAndSavePDFRequest request) {
        if (request == null) {
            return Optional.empty();
        }

        GeneratedPDFEntity entity = new GeneratedPDFEntity();
        entity.setFileName(request.getFileName());
        entity.setNumberOfPages(request.getNumberOfPages());
        entity.setUserId(request.getUserId());
        entity.setEncodedPdf(PLACEHOLDER_CONTENT);
        return Optional.of(entity);
    }

     public GeneratePDFResponse map(GeneratedPDFEntity entity) {
        GeneratePDFResponse response = new GeneratePDFResponse();
        response.setId(String.valueOf(entity.getId()));
        response.setFileName(entity.getFileName());
        response.setContent(entity.getEncodedPdf());
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        return response;
    }
}
