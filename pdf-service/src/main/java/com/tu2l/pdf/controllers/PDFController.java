package com.tu2l.pdf.controllers;

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
    private final PDFService pdfService;

    @Autowired
    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse> generateResume(@RequestBody GeneratePDFRequest request) throws Exception {
        return ResponseEntity.ok(pdfService.generate(request));
    }
}