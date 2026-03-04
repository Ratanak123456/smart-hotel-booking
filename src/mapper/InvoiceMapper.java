package mapper;

import dto.InvoiceDTO;
import model.entities.Invoice;

public class InvoiceMapper {
    public static InvoiceDTO toDTO(Invoice invoice) {
        if (invoice == null) return null;
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setGuestName(invoice.getGuestName());
        dto.setRoomNumber(invoice.getRoomNumber());
        dto.setCheckInDate(invoice.getCheckInDate());
        dto.setCheckOutDate(invoice.getCheckOutDate());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getInvoiceStatus().toString());
        return dto;
    }
}
