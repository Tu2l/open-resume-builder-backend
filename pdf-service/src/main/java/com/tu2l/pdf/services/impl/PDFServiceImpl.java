package com.tu2l.pdf.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tu2l.common.models.states.ResponseProcessingStatus;
import com.tu2l.common.utils.Util;
import com.tu2l.pdf.entities.GeneratedPDFEntity;
import com.tu2l.pdf.exception.PDFException;
import com.tu2l.pdf.generators.PDFGenerator;
import com.tu2l.pdf.generators.PDFGeneratorConfiguration;
import com.tu2l.pdf.generators.PDFGeneratorConfiguration.LayoutParams;
import com.tu2l.pdf.models.GenerateAndSavePDFRequest;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;
import com.tu2l.pdf.repositories.PDFRepository;
import com.tu2l.pdf.services.PDFService;
import com.tu2l.pdf.utils.EntityMapper;

@Service
public class PDFServiceImpl implements PDFService {
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);
    private final PDFRepository repository;
    private final Util util;
    private final EntityMapper mapper;
    private final PDFGenerator pdfGenerator;
    private final AsyncPDFService asyncPDFService;

    @Autowired
    public PDFServiceImpl(PDFRepository repository, Util util, EntityMapper mapper,
            @Qualifier("wkhtmlToPdfGenerator") PDFGenerator pdfGenerator,
            AsyncPDFService asyncPDFService) {
        this.repository = repository;
        this.util = util;
        this.mapper = mapper;
        this.pdfGenerator = pdfGenerator;
        this.asyncPDFService = asyncPDFService;
    }

    @Override
    public GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception {
        logger.debug("Starting PDF generation: fileName={}, numberOfPages={}", pdfRequest.getFileName(),
                pdfRequest.getNumberOfPages());
        return generateAndGetPDFResponse(pdfRequest);
    }

    @Override
    public GeneratePDFResponse generateAndSave(GenerateAndSavePDFRequest pdfRequest) throws Exception {
        logger.info("Starting PDF generation and save: fileName={}", pdfRequest.getFileName());

        GeneratePDFResponse response = generateAndGetPDFResponse(pdfRequest);
        GeneratedPDFEntity entity = mapper.map(response, pdfRequest)
                .orElseThrow(() -> new PDFException("Mapping to entity failed"));
        if (entity != null && entity.getEncodedPdf() != null) {
            repository.save(entity);
        }

        return response;
    }

    @Override
    public GeneratePDFResponse generateAsync(GenerateAndSavePDFRequest pdfRequest) throws Exception {
        logger.info("Starting asynchronous PDF generation: fileName={}", pdfRequest.getFileName());
        GeneratePDFResponse asyncResponse = new GeneratePDFResponse();

        GeneratedPDFEntity pdfToBeGenerated = mapper.map(pdfRequest)
                .orElseThrow(() -> new PDFException("Mapping to entity failed"));

        GeneratedPDFEntity saved = repository.save(pdfToBeGenerated);
        asyncResponse.setId(String.valueOf(saved.getId()));
        asyncResponse.setFileName(saved.getFileName());
        asyncResponse.setStatus(ResponseProcessingStatus.PROCESSING);

        // Delegate to AsyncPDFService where @Async will work via Spring proxy
        asyncPDFService.processAsyncPDFGeneration(pdfRequest, saved.getId(), this::generateAndGetPDFResponse);

        return asyncResponse;
    }

    @Override
    public GeneratePDFResponse getGeneratedPDFById(long pdfRequestId) throws Exception {
        logger.info("Fetching generated PDF by ID: pdfRequestId={}", pdfRequestId);
        return repository.findById(pdfRequestId)
                .map(mapper::map)
                .orElseThrow(() -> new PDFException("Generated PDF not found for ID: " + pdfRequestId));
    }

    private GeneratePDFResponse generateAndGetPDFResponse(GeneratePDFRequest pdfRequest) throws Exception {
        GeneratePDFResponse response = new GeneratePDFResponse();

        logger.debug("Decoding and Sanitizing content for file: {}", pdfRequest.getFileName());

        String content = util.decodeAndSanitizeBase64StringToString(pdfRequest.getContent());
        String fileName = util.cleanExtension(pdfRequest.getFileName());

        logger.debug("Cleaned filename: {}", fileName);

        PDFGeneratorConfiguration configuration = PDFGeneratorConfiguration.builder()
                .htmlContent(content)
                .fileName(fileName)
                .numberOfPagesToGenerate(pdfRequest.getNumberOfPages())
                .layoutParams(new LayoutParams())
                .build();

        byte[] pdfBytes = pdfGenerator.generatePDF(configuration);

        logger.info("PDF generated successfully: fileName={}, size={} bytes", fileName, pdfBytes.length);

        content = util.encodeByteArrayToBase64String(pdfBytes);
        response.setContent(content);
        response.setFileName(fileName);
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        return response;
    }
}
