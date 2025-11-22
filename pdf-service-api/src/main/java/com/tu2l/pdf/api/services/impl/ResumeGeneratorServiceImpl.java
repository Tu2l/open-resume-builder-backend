package com.tu2l.pdf.api.services.impl;

import com.tu2l.pdf.api.models.GenerateResumeRequest;
import com.tu2l.pdf.api.models.GenerateResumeResponse;
import com.tu2l.pdf.api.models.states.ResponseProcessingStatus;
import com.tu2l.pdf.api.services.PDFService;
import com.tu2l.pdf.api.services.ResumeGeneratorService;
import com.tu2l.pdf.api.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for generating resumes.
 */
@Service
public class ResumeGeneratorServiceImpl
        implements ResumeGeneratorService<GenerateResumeRequest, GenerateResumeResponse> {

    private final Util util;
    private final PDFService pdfService;

    @Autowired
    public ResumeGeneratorServiceImpl(Util util, PDFService pdfService) {
        this.util = util;
        this.pdfService = pdfService;
    }

    @Override
    public GenerateResumeResponse generateResume(GenerateResumeRequest resumeData) {
        GenerateResumeResponse response = new GenerateResumeResponse();

        try {
            String content = util.decodeBase64StringToString(resumeData.getContent());
            byte[] pdfBytes = pdfService.generatePdf(content, resumeData.getFileName(), resumeData.getNumberOfPages());
            content = util.encodeByteArrayToBase64String(pdfBytes);
            response.setContent(content);
            response.setStatus(ResponseProcessingStatus.SUCCESS);
        } catch (Exception e) {
            response.setStatus(ResponseProcessingStatus.FAILURE);
            response.setMessage("Error generating PDF: " + e.getMessage());
            return response;
        }
        
        return response;
    }
}
