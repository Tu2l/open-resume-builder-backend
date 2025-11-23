package com.tu2l.pdf.generators;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PDFGeneratorConfiguration {
    private LayoutParams layoutParams;
    private int numberOfPagesToGenerate;
    private String fileName;
    private String htmlContent;

    @Data
    public static class LayoutParams {
        private boolean includeHeader;
        private PageSize pageSize = PageSize.A4;
        private Orientation orientation = Orientation.PORTRAIT;
        private String marginTop = "6mm";
        private String marginBottom = "6mm";
        private String marginLeft = "8mm";
        private String marginRight = "8mm";
    }

    public static enum Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    public static enum PageSize {
        A4,
        LETTER,
        LEGAL
    }

}