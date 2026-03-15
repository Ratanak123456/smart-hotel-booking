package com.lowagie.text.pdf;

/**
 * Fake class to satisfy JasperReports 6.21.0 dependency on unpatched iText.
 */
public class FopGlyphProcessor {
    public static boolean isFopSupported() {
        return false;
    }
}
