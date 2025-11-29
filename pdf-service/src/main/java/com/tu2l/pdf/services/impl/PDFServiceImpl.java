package com.tu2l.pdf.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tu2l.common.models.states.ResponseProcessingStatus;
import com.tu2l.common.utils.Util;
import com.tu2l.pdf.entities.GeneratedPDFEntity;
import com.tu2l.pdf.generators.PDFGenerator;
import com.tu2l.pdf.generators.PDFGeneratorConfiguration;
import com.tu2l.pdf.generators.PDFGeneratorConfiguration.LayoutParams;
import com.tu2l.pdf.models.GenerateAndSavePDFRequest;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;
import com.tu2l.pdf.repositories.PDFRepository;
import com.tu2l.pdf.services.PDFService;
import com.tu2l.pdf.utils.RequestToEntityMapper;

@Service
public class PDFServiceImpl implements PDFService {
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);
    private final PDFRepository repository;
    private final Util util;
    private final RequestToEntityMapper mapper;
    private final PDFGenerator pdfGenerator;

    @Autowired
    public PDFServiceImpl(PDFRepository repository, Util util, RequestToEntityMapper mapper,
            @Qualifier("wkhtmlToPdfGenerator") PDFGenerator pdfGenerator) {
        this.repository = repository;
        this.util = util;
        this.mapper = mapper;
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception {
        logger.debug("Starting PDF generation: fileName={}, numberOfPages={}",
                pdfRequest.getFileName(), pdfRequest.getNumberOfPages());
        GeneratePDFResponse response = new GeneratePDFResponse();

        logger.debug("Decoding content for file: {}", pdfRequest.getFileName());
        String content = util.decodeBase64StringToString(pdfRequest.getContent());

        // Sanitize HTML content to prevent security issues
        logger.debug("Sanitizing HTML content for file: {}", pdfRequest.getFileName());
        content = util.sanitizeHtml(content);

        String fileName = util.cleanExtension(pdfRequest.getFileName());
        logger.debug("Cleaned filename: {}", fileName);

        PDFGeneratorConfiguration configuration = PDFGeneratorConfiguration.builder()
                .htmlContent(content)
                .fileName(fileName)
                .numberOfPagesToGenerate(pdfRequest.getNumberOfPages())
                .layoutParams(new LayoutParams())
                .build();

        byte[] pdfBytes = pdfGenerator.generatePDF(configuration);

        logger.info("PDF generated successfully: fileName={}, size={} bytes",
                fileName, pdfBytes.length);

        content = util.encodeByteArrayToBase64String(pdfBytes);
        response.setContent(content);
        response.setFileName(fileName);
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        return response;
    }

    @Override
    public GeneratePDFResponse generateAndSave(GenerateAndSavePDFRequest pdfRequest) throws Exception {
        logger.info("Starting PDF generation and save: fileName={}", pdfRequest.getFileName());

        GeneratePDFResponse response = generate(pdfRequest);
        GeneratedPDFEntity entity = mapper.map(response, pdfRequest);
        if (entity != null && entity.getEncodedPdf() != null) {
            repository.save(entity);
        }

        return response;
    }
}
