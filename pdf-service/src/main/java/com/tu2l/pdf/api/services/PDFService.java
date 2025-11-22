package com.tu2l.pdf.api.services;

/**
 * Service interface for generating PDFs from HTML content.
 */
public interface PDFService {
    byte[] generatePdf(String html, String fileName, int numberOfPages) throws Exception;
}
