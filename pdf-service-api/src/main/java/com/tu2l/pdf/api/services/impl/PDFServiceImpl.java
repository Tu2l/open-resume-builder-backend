package com.tu2l.pdf.api.services.impl;

import com.tu2l.pdf.api.services.PDFService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class PDFServiceImpl implements PDFService {
    private static final String JAVA_TEMP_DIR = "java.io.tmpdir";


    @Override
    public byte[] generatePdf(String html, String fileName, int numberOfPages) throws Exception {
        // Create temporary files for HTML input and PDF output
        String tempDir = System.getProperty(JAVA_TEMP_DIR) + File.separator + System.currentTimeMillis();
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
