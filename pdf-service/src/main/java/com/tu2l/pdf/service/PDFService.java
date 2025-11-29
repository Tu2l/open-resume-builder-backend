package com.tu2l.pdf.service;

import com.tu2l.pdf.model.request.GenerateAndSavePDFRequest;
import com.tu2l.pdf.model.request.GeneratePDFRequest;
import com.tu2l.pdf.model.response.GeneratePDFResponse;

/**
 * Service interface for generating PDFs from HTML content.
 */
public interface PDFService {
    // Synchronous PDF generation
    GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception;
    GeneratePDFResponse generateAndSave(GenerateAndSavePDFRequest pdfRequest) throws Exception;

    // Asynchronous PDF generation
    GeneratePDFResponse generateAsync(GenerateAndSavePDFRequest pdfRequest) throws Exception;
    GeneratePDFResponse getGeneratedPDFById(long pdfRequestId) throws Exception;
}
