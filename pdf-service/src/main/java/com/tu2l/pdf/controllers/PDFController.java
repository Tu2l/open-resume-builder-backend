package com.tu2l.pdf.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.common.models.base.BaseResponse;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.services.PDFService;

@RestController
@RequestMapping("/pdf")
public class PDFController {
    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);
    private final PDFService pdfService;

    @Autowired
    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse> generate(@RequestBody GeneratePDFRequest request) throws Exception {
        logger.info("Received request to generate PDF: fileName={}", request.getFileName());
        try {
            BaseResponse response = pdfService.generate(request);
            logger.info("PDF generation completed successfully: fileName={}, status={}", 
                request.getFileName(), response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating PDF: fileName={}", request.getFileName(), e);
            throw e;
        }
    }

    @PostMapping("/generate/save") 
    public ResponseEntity<BaseResponse> generateAndSave(@RequestBody GeneratePDFRequest request) throws Exception {
        logger.info("Received request to generate and save PDF: fileName={}", request.getFileName());
        try {
            BaseResponse response = pdfService.generateAndSave(request);
            logger.info("PDF generation and save completed successfully: fileName={}, status={}", 
                request.getFileName(), response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating and saving PDF: fileName={}", request.getFileName(), e);
            throw e;
        }
    }

}