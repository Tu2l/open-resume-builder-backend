package com.tu2l.pdf.generators;

/**
 * Interface defining methods for PDF generation.
 * Implementations should handle initialization with configuration
 * and the actual PDF generation from HTML content.
 */
public interface PDFGenerator {
    byte[] generatePDF(PDFGeneratorConfiguration configuration) throws Exception;
}
