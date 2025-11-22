package com.tu2l.pdf.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu2l.common.models.states.ResponseProcessingStatus;
import com.tu2l.common.utils.Util;
import com.tu2l.pdf.models.GeneratePDFRequest;
import com.tu2l.pdf.models.GeneratePDFResponse;
import com.tu2l.pdf.services.PDFService;

@Service
public class PDFServiceImpl implements PDFService {
    private static final String JAVA_TEMP_DIR = "java.io.tmpdir";
    private final Util util;

    @Autowired
    public PDFServiceImpl(Util util) {
        this.util = util;
    }

    @Override
    public GeneratePDFResponse generate(GeneratePDFRequest pdfRequest) throws Exception {
        GeneratePDFResponse response = new GeneratePDFResponse();

        try {
            String content = util.decodeBase64StringToString(pdfRequest.getContent());
            String fileName = util.cleanExtension(pdfRequest.getFileName());
            byte[] pdfBytes = generatePdf(content, fileName, pdfRequest.getNumberOfPages());
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

    byte[] generatePdf(String html, String fileName, int numberOfPages) throws Exception {
        // Create temporary files for HTML input and PDF output
        String tempDir = System.getProperty(JAVA_TEMP_DIR) + File.separator + System.currentTimeMillis();
        boolean result = new File(tempDir).mkdirs();

        if (!result) {
            throw new IOException("Failed to create temp directory: " + tempDir);
        }

        File htmlFile = new File(tempDir, fileName + ".html");
        File pdfFile = new File(tempDir, fileName + ".pdf");

        try {
            // Write HTML to temp file
            Files.writeString(htmlFile.toPath(), html);

            // Execute wkhtmltopdf command
            int exitCode = executeWkhtmltopdfCommand(htmlFile, pdfFile);

            if (exitCode != 0) {
                throw new RuntimeException("wkhtmltopdf failed with exit code: " + exitCode);
            }

            // Read the generated PDF
            return Files.readAllBytes(pdfFile.toPath());

        } finally {
            // Clean up temp files
            cleanUpResidualFiles(htmlFile, pdfFile);
        }
    }

    private void cleanUpResidualFiles(File htmlFile, File pdfFile) {
        if (htmlFile.exists())
            htmlFile.delete();
        if (pdfFile.exists())
            pdfFile.delete();
    }

    private int executeWkhtmltopdfCommand(File htmlFile, File pdfFile) throws IOException, InterruptedException {
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
        return process.waitFor();
    }
}
