package com.tu2l.pdf.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

import com.tu2l.pdf.generator.PDFGeneratorConfiguration.LayoutParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("wkhtmlToPdfGenerator")
public class WkhtmlToPdfGenerator implements PDFGenerator {
    private static final String JAVA_TEMP_DIR = "java.io.tmpdir";

    @Override
    public byte[] generatePDF(PDFGeneratorConfiguration configuration) throws Exception {
        // Implementation for generating PDF using wkhtmltopdf
        // This is a placeholder implementation
        if (configuration == null) {
            throw new IllegalStateException("PDFGenerator is not initialized with configuration.");
        }

        // Create temporary files for HTML input and PDF output
        String tempDir = System.getProperty(JAVA_TEMP_DIR) + File.separator + System.currentTimeMillis();
        File tempDirFile = new File(tempDir);
        boolean result = tempDirFile.mkdirs();

        if (!result) {
            log.error("Failed to create temp directory: {}", tempDir);
            throw new IOException("Failed to create temp directory: " + tempDir);
        }
        log.debug("Temporary directory created: {}", tempDir);

        File htmlFile = new File(tempDir, configuration.getFileName() + ".html");
        File pdfFile = new File(tempDir, configuration.getFileName() + ".pdf");

        try {
            // Write HTML to temp file
            log.debug("Writing HTML content to file: {}", htmlFile.getAbsolutePath());
            Files.writeString(htmlFile.toPath(), configuration.getHtmlContent());

            // Execute wkhtmltopdf command
            log.debug("Executing wkhtmltopdf command for file: {}", configuration.getFileName());
            int exitCode = executeWkhtmltopdfCommand(htmlFile, pdfFile, configuration.getLayoutParams());

            if (exitCode != 0) {
                log.error("wkhtmltopdf failed with exit code: {} for file: {}", exitCode,
                        configuration.getFileName());
                throw new RuntimeException("wkhtmltopdf failed with exit code: " + exitCode);
            }
            log.debug("wkhtmltopdf command completed successfully for file: {}", configuration.getFileName());

            // Read the generated PDF
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            log.debug("PDF file read successfully: fileName={}, size={} bytes", configuration.getFileName(),
                    pdfBytes.length);
            return pdfBytes;

        } finally {
            // Clean up temp files and directory
            log.debug("Cleaning up temporary files and directory for: {}", configuration.getFileName());
            cleanUpResidualFiles(htmlFile, pdfFile);
            if (tempDirFile.exists()) {
                boolean deleted = tempDirFile.delete();
                log.debug("Temp directory cleanup: {}, deleted={}", tempDirFile.getName(), deleted);
            }
        }
    }

    private int executeWkhtmltopdfCommand(File htmlFile, File pdfFile, LayoutParams layoutParams)
            throws IOException, InterruptedException {
        log.debug("Building wkhtmltopdf command: input={}, output={}",
                htmlFile.getAbsolutePath(), pdfFile.getAbsolutePath());
        ProcessBuilder processBuilder = new ProcessBuilder(
                "wkhtmltopdf",
                "--page-size", layoutParams.getPageSize().name(),
                "--margin-top", layoutParams.getMarginTop(),
                "--margin-bottom", layoutParams.getMarginBottom(),
                "--margin-left", layoutParams.getMarginLeft(),
                "--margin-right", layoutParams.getMarginRight(),
                htmlFile.getAbsolutePath(),
                pdfFile.getAbsolutePath());

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        log.debug("wkhtmltopdf process started, waiting for completion");
        int exitCode = process.waitFor();
        log.debug("wkhtmltopdf process completed with exit code: {}", exitCode);
        return exitCode;
    }

    private void cleanUpResidualFiles(File htmlFile, File pdfFile) {
        if (htmlFile.exists()) {
            boolean deleted = htmlFile.delete();
            log.debug("HTML file cleanup: {}, deleted={}", htmlFile.getName(), deleted);
        }
        if (pdfFile.exists()) {
            boolean deleted = pdfFile.delete();
            log.debug("PDF file cleanup: {}, deleted={}", pdfFile.getName(), deleted);
        }
    }
}
