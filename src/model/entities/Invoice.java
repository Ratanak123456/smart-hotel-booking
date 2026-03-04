package model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    private int id;
    private int bookingId;
    private String invoiceNumber;
    private LocalDate issueDate;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String roomNumber;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private BigDecimal pricePerNight;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private InvoiceStatus invoiceStatus;
    private Timestamp createdAt;

    public enum InvoiceStatus {
        PENDING, PAID
    }
}
