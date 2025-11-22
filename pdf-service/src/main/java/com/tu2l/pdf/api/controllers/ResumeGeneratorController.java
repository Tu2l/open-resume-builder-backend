package com.tu2l.pdf.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.pdf.api.models.GenerateResumeRequest;
import com.tu2l.pdf.api.models.GenerateResumeResponse;
import com.tu2l.pdf.api.models.base.BaseResponse;
import com.tu2l.pdf.api.services.ResumeGeneratorService;

@RestController
@RequestMapping("/resume")
public class ResumeGeneratorController {
    
    private final ResumeGeneratorService<GenerateResumeRequest, GenerateResumeResponse> resumeGeneratorService;

    @Autowired
    public ResumeGeneratorController(ResumeGeneratorService<GenerateResumeRequest, GenerateResumeResponse> resumeGeneratorService) {
        this.resumeGeneratorService = resumeGeneratorService;
    }

    @PostMapping()
    public ResponseEntity<BaseResponse> generateResume(@RequestBody GenerateResumeRequest request) {
        return ResponseEntity.ok(resumeGeneratorService.generateResume(request));
    }
}