package com.tu2l.pdf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.pdf.model.request.GenerateAndSavePDFRequest;
import com.tu2l.pdf.model.request.GeneratePDFRequest;
import com.tu2l.pdf.service.PDFService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/")
public class PDFController {
    private final PDFService pdfService;

    @Autowired
    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse> generate(@Valid @RequestBody GeneratePDFRequest request) throws Exception {
        log.info("Received request to generate PDF: fileName={}", request.getFileName());
        BaseResponse response = pdfService.generate(request);
        log.info("PDF generation completed successfully: fileName={}, status={}", request.getFileName(),
                response.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/save")
    public ResponseEntity<BaseResponse> generateAndSave(@Valid @RequestBody GenerateAndSavePDFRequest request)
            throws Exception {
        log.info("Received request to generate and save PDF: fileName={}", request.getFileName());

        BaseResponse response = pdfService.generateAndSave(request);
        log.info("PDF generation and save completed successfully: fileName={}, status={}",
                request.getFileName(), response.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/async")
    public ResponseEntity<BaseResponse> generateAndSaveAsync(@Valid @RequestBody GenerateAndSavePDFRequest request)
            throws Exception {
        log.info("Received async request to generate and save PDF: fileName={}", request.getFileName()); 
        BaseResponse response = pdfService.generateAsync(request);
        log.info("Asynchronous PDF generation and save initiated: fileName={}, status={}, fileId={}",
                request.getFileName(), response.getStatus(), response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/id/{id}")
    public ResponseEntity<BaseResponse> getPdfById(@PathVariable("id") long id) throws Exception {
        log.info("Received request to get PDF by id: pdfId={}", id);
        return ResponseEntity.ok(pdfService.getGeneratedPDFById(id));
    }    

}