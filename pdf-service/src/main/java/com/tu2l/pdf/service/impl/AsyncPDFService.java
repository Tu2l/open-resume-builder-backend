package com.tu2l.pdf.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tu2l.pdf.entity.GeneratedPDFEntity;
import com.tu2l.pdf.exception.PDFException;
import com.tu2l.pdf.model.request.GenerateAndSavePDFRequest;
import com.tu2l.pdf.model.request.GeneratePDFRequest;
import com.tu2l.pdf.model.response.GeneratePDFResponse;
import com.tu2l.pdf.repository.PDFRepository;
import com.tu2l.pdf.util.EntityMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Async processing service that handles asynchronous PDF generation
 * in a separate class, allowing Spring's @Async proxy to work correctly.
 */
@Slf4j
@Service
public class AsyncPDFService {
    private final PDFRepository repository;
    private final EntityMapper mapper;

    public AsyncPDFService(PDFRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Async
    public void processAsyncPDFGeneration(
            GenerateAndSavePDFRequest pdfRequest,
            Long savedEntityId,
            PDFGenerationFunction pdfGenerationFunction) {
        log.info("Processing async PDF in thread: {}", Thread.currentThread().getName());
        try {
            GeneratePDFResponse response = pdfGenerationFunction.generate(pdfRequest);
            GeneratedPDFEntity entityToUpdate = mapper.map(response, pdfRequest)
                    .orElseThrow(() -> new PDFException("Mapping to entity failed"));
            entityToUpdate.setId(savedEntityId);
            repository.save(entityToUpdate);
            log.info("Asynchronous PDF generation completed: fileName={}, thread={}",
                    pdfRequest.getFileName(), Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("Error during asynchronous PDF generation: fileName={}, error={}",
                    pdfRequest.getFileName(), e.getMessage(), e);

            // Note: Update entity status/error handling can be added when entity supports
            // it
        }
    }

    /**
     * Functional interface for PDF generation operations.
     * Allows passing PDF generation logic as a parameter while handling checked
     * exceptions.
     */
    @FunctionalInterface
    public interface PDFGenerationFunction {
        GeneratePDFResponse generate(GeneratePDFRequest request) throws Exception;
    }
}
