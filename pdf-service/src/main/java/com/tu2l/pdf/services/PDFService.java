package com.tu2l.pdf.services;

import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;

/**
 * Service interface for generating PDFs from HTML content.
 */
public interface PDFService {
    GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception;
}
