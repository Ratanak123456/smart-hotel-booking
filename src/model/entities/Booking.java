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
public class Booking {
    private int id;
    private int userId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Long telegramChatId;
    private Timestamp createdAt;

    // Additional fields for display
    private String roomNumber;
    private String roomTypeName;
    private String username;
    private String userEmail;
    private String userPhone;

    public enum BookingStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED
    }
}
