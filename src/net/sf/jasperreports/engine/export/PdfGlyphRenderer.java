package net.sf.jasperreports.engine.export;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.export.pdf.PdfProducer;

/**
 * SILENT version of PdfGlyphRenderer to suppress the "Unpatched PDF library" warning.
 * We return false for supported() silently.
 */
public class PdfGlyphRenderer extends AbstractPdfTextRenderer {

    public static boolean supported() {
        // Return false silently to avoid JasperReports trying to use it
        return false;
    }

    public PdfGlyphRenderer(JasperReportsContext jasperReportsContext, boolean ignoreMissingFont, 
                            boolean defaultIndentFirstLine, boolean defaultJustifyLastLine) {
        super(jasperReportsContext, ignoreMissingFont, defaultIndentFirstLine, defaultJustifyLastLine);
    }

    @Override
    public void initialize(JRPdfExporter pdfExporter, PdfProducer pdfProducer, JRPdfExporterTagHelper tagHelper, 
                           JRPrintText text, JRStyledText styledText, int offsetX, int offsetY) {
        // Do nothing
    }

    @Override
    public void render() {
        // Do nothing
    }

    @Override
    public void draw() {
        // Do nothing
    }

    @Override
    public boolean addActualText() {
        return false;
    }
}
