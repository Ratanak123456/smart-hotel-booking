package model.service;

import model.entities.Invoice;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class InvoicePdfService {

    public String generateInvoicePdf(Invoice invoice, String outputPath) throws Exception {
        // Load the report template from resources
        InputStream templateStream = getClass().getClassLoader()
                .getResourceAsStream("invoice.jrxml");

        if (templateStream == null) {
            throw new RuntimeException("Invoice template not found in resources");
        }

        // Compile the report
        JasperReport report = JasperCompileManager.compileReport(templateStream);

        // Create parameters map
        Map<String, Object> params = new HashMap<>();
        params.put("InvoiceNumber", invoice.getInvoiceNumber());
        params.put("IssueDate", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "");
        params.put("GuestName", invoice.getGuestName());
        params.put("GuestEmail", invoice.getGuestEmail());
        params.put("GuestPhone", invoice.getGuestPhone());
        params.put("RoomNumber", invoice.getRoomNumber());
        params.put("RoomTypeName", invoice.getRoomTypeName());
        params.put("CheckInDate", invoice.getCheckInDate() != null ? invoice.getCheckInDate().toString() : "");
        params.put("CheckOutDate", invoice.getCheckOutDate() != null ? invoice.getCheckOutDate().toString() : "");
        params.put("Nights", invoice.getNights());
        params.put("PricePerNight", invoice.getPricePerNight() != null ? invoice.getPricePerNight() : BigDecimal.ZERO);
        params.put("DiscountAmount", invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO);
        params.put("TotalAmount", invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO);
        params.put("Status", invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus().name() : "UNKNOWN");

        // Fill the report
        JasperPrint print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());

        // Ensure output directory exists
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Export to PDF
        String fileName = outputPath + File.separator + "invoice_" + invoice.getInvoiceNumber() + ".pdf";
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new FileOutputStream(fileName)));
        exporter.exportReport();

        return fileName;
    }
}