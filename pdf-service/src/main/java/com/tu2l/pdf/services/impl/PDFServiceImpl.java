package com.tu2l.pdf.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu2l.common.models.states.ResponseProcessingStatus;
import com.tu2l.common.utils.Util;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;
import com.tu2l.pdf.services.PDFService;

@Service
public class PDFServiceImpl implements PDFService {
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);
    private static final String JAVA_TEMP_DIR = "java.io.tmpdir";
    private final Util util;

    @Autowired
    public PDFServiceImpl(Util util) {
        this.util = util;
    }

    @Override
    public GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception {
        logger.debug("Starting PDF generation: fileName={}, numberOfPages={}", 
            pdfRequest.getFileName(), pdfRequest.getNumberOfPages());
        GeneratePDFResponse response = new GeneratePDFResponse();

        try {
            logger.debug("Decoding content for file: {}", pdfRequest.getFileName());
            String content = util.decodeBase64StringToString(pdfRequest.getContent());
            String fileName = util.cleanExtension(pdfRequest.getFileName());
            logger.debug("Cleaned filename: {}", fileName);
            
            byte[] pdfBytes = generatePdf(content, fileName, pdfRequest.getNumberOfPages());
            logger.info("PDF generated successfully: fileName={}, size={} bytes", 
                fileName, pdfBytes.length);
            
            content = util.encodeByteArrayToBase64String(pdfBytes);
            response.setContent(content);
            response.setStatus(ResponseProcessingStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("Failed to generate PDF: fileName={}", pdfRequest.getFileName(), e);
            response.setStatus(ResponseProcessingStatus.FAILURE);
            response.setMessage("Error generating PDF: " + e.getMessage());
            return response;
        }

        return response;
    }

    @Override
    public GeneratePDFResponse generateAndSave(GeneratePDFRequest pdfRequest) throws Exception {
        logger.info("Starting PDF generation and save: fileName={}", pdfRequest.getFileName());
        // Implementation for generating and saving the PDF
        // This is a placeholder; 
        // TODO: actual saving logic would depend on requirements
        return generate(pdfRequest);
    }


    byte[] generatePdf(String html, String fileName, int numberOfPages) throws Exception {
        logger.debug("Creating temporary directory for PDF generation: fileName={}", fileName);
        // Create temporary files for HTML input and PDF output
        String tempDir = System.getProperty(JAVA_TEMP_DIR) + File.separator + System.currentTimeMillis();
        boolean result = new File(tempDir).mkdirs();

        if (!result) {
            logger.error("Failed to create temp directory: {}", tempDir);
            throw new IOException("Failed to create temp directory: " + tempDir);
        }
        logger.debug("Temporary directory created: {}", tempDir);

        File htmlFile = new File(tempDir, fileName + ".html");
        File pdfFile = new File(tempDir, fileName + ".pdf");

        try {
            // Write HTML to temp file
            logger.debug("Writing HTML content to file: {}", htmlFile.getAbsolutePath());
            Files.writeString(htmlFile.toPath(), html);

            // Execute wkhtmltopdf command
            logger.debug("Executing wkhtmltopdf command for file: {}", fileName);
            int exitCode = executeWkhtmltopdfCommand(htmlFile, pdfFile);

            if (exitCode != 0) {
                logger.error("wkhtmltopdf failed with exit code: {} for file: {}", exitCode, fileName);
                throw new RuntimeException("wkhtmltopdf failed with exit code: " + exitCode);
            }
            logger.debug("wkhtmltopdf command completed successfully for file: {}", fileName);

            // Read the generated PDF
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            logger.debug("PDF file read successfully: fileName={}, size={} bytes", fileName, pdfBytes.length);
            return pdfBytes;

        } finally {
            // Clean up temp files
            logger.debug("Cleaning up temporary files for: {}", fileName);
            cleanUpResidualFiles(htmlFile, pdfFile);
        }
    }

    private void cleanUpResidualFiles(File htmlFile, File pdfFile) {
        if (htmlFile.exists()) {
            boolean deleted = htmlFile.delete();
            logger.debug("HTML file cleanup: {}, deleted={}", htmlFile.getName(), deleted);
        }
        if (pdfFile.exists()) {
            boolean deleted = pdfFile.delete();
            logger.debug("PDF file cleanup: {}, deleted={}", pdfFile.getName(), deleted);
        }
    }

    private int executeWkhtmltopdfCommand(File htmlFile, File pdfFile) throws IOException, InterruptedException {
        logger.debug("Building wkhtmltopdf command: input={}, output={}", 
            htmlFile.getAbsolutePath(), pdfFile.getAbsolutePath());
        ProcessBuilder processBuilder = new ProcessBuilder(
                "wkhtmltopdf",
                "--enable-local-file-access",
                "--page-size", "A4",
                "--margin-top", "12mm",
                "--margin-bottom", "12mm",
                "--margin-left", "15mm",
                "--margin-right", "15mm",
                htmlFile.getAbsolutePath(),
                pdfFile.getAbsolutePath());

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        logger.debug("wkhtmltopdf process started, waiting for completion");
        int exitCode = process.waitFor();
        logger.debug("wkhtmltopdf process completed with exit code: {}", exitCode);
        return exitCode;
    }
}
