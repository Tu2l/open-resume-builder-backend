package com.tu2l.pdf.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tu2l.pdf.entities.GeneratedPDFEntity;
import com.tu2l.pdf.exception.PDFException;
import com.tu2l.pdf.models.GenerateAndSavePDFRequest;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;
import com.tu2l.pdf.repositories.PDFRepository;
import com.tu2l.pdf.utils.EntityMapper;

/**
 * Async processing service that handles asynchronous PDF generation
 * in a separate class, allowing Spring's @Async proxy to work correctly.
 */
@Service
public class AsyncPDFService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncPDFService.class);
    private final PDFRepository repository;
    private final EntityMapper mapper;

    @Autowired
    public AsyncPDFService(PDFRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Async
    public void processAsyncPDFGeneration(
            GenerateAndSavePDFRequest pdfRequest, 
            Long savedEntityId, 
            PDFGenerationFunction pdfGenerationFunction) {
        logger.info("Processing async PDF in thread: {}", Thread.currentThread().getName());
        try {
            GeneratePDFResponse response = pdfGenerationFunction.generate(pdfRequest);
            GeneratedPDFEntity entityToUpdate = mapper.map(response, pdfRequest)
                    .orElseThrow(() -> new PDFException("Mapping to entity failed"));
            entityToUpdate.setId(savedEntityId);
            repository.save(entityToUpdate);
            logger.info("Asynchronous PDF generation completed: fileName={}, thread={}", 
                pdfRequest.getFileName(), Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("Error during asynchronous PDF generation: fileName={}, error={}",
                    pdfRequest.getFileName(), e.getMessage(), e);
            
            // Note: Update entity status/error handling can be added when entity supports it
        }
    }

    /**
     * Functional interface for PDF generation operations.
     * Allows passing PDF generation logic as a parameter while handling checked exceptions.
     */
    @FunctionalInterface
    public interface PDFGenerationFunction {
        GeneratePDFResponse generate(GeneratePDFRequest request) throws Exception;
    }
}
