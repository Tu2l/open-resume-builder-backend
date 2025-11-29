package com.tu2l.pdf.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.common.models.base.BaseResponse;
import com.tu2l.pdf.models.GenerateAndSavePDFRequest;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.services.PDFService;

import jakarta.validation.Valid;

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
    public ResponseEntity<BaseResponse> generate(@Valid @RequestBody GeneratePDFRequest request) throws Exception {
        logger.info("Received request to generate PDF: fileName={}", request.getFileName());
        BaseResponse response = pdfService.generate(request);
        logger.info("PDF generation completed successfully: fileName={}, status={}", request.getFileName(),
                response.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/save")
    public ResponseEntity<BaseResponse> generateAndSave(@Valid @RequestBody GenerateAndSavePDFRequest request)
            throws Exception {
        logger.info("Received request to generate and save PDF: fileName={}", request.getFileName());

        BaseResponse response = pdfService.generateAndSave(request);
        logger.info("PDF generation and save completed successfully: fileName={}, status={}",
                request.getFileName(), response.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/async")
    public ResponseEntity<BaseResponse> generateAndSaveAsync(@Valid @RequestBody GenerateAndSavePDFRequest request)
            throws Exception {
        logger.info("Received async request to generate and save PDF: fileName={}", request.getFileName()); 
        BaseResponse response = pdfService.generateAsync(request);
        logger.info("Asynchronous PDF generation and save initiated: fileName={}, status={}, fileId={}",
                request.getFileName(), response.getStatus(), response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/id/{id}")
    public ResponseEntity<BaseResponse> getPdfById(@PathVariable("id") long id) throws Exception {
        logger.info("Received request to get PDF by id: pdfId={}", id);
        return ResponseEntity.ok(pdfService.getGeneratedPDFById(id));
    }    

}