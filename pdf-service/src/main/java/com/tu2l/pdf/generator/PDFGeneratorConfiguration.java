package com.tu2l.pdf.generator;

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
        public static final String DEFAULT_TOP_MARGIN = "6mm";
        public static final String DEFAULT_BOTTOM_MARGIN = "6mm";
        public static final String DEFAULT_LEFT_MARGIN = "8mm";
        public static final String DEFAULT_RIGHT_MARGIN = "8mm";

        private boolean includeHeader = false;
        private PageSize pageSize = PageSize.A4;
        private Orientation orientation = Orientation.PORTRAIT;
        private String marginTop = DEFAULT_TOP_MARGIN;
        private String marginBottom = DEFAULT_BOTTOM_MARGIN;
        private String marginLeft = DEFAULT_LEFT_MARGIN;
        private String marginRight = DEFAULT_RIGHT_MARGIN;
    }

    public static enum Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    public static enum PageSize {
        // ISO A Series
        A0,
        A1,
        A2,
        A3,
        A4,
        A5,
        A6,
        A7,
        A8,
        A9,
        
        // ISO B Series
        B0,
        B1,
        B2,
        B3,
        B4,
        B5,
        B6,
        B7,
        B8,
        B9,
        B10,
        
        // ISO C Series (Envelopes)
        C5E,
        COMM10E,
        DLE,
        
        // North American
        EXECUTIVE,
        FOLIO,
        LEDGER,
        LEGAL,
        LETTER,
        TABLOID
    }

}