package model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private int id;
    private String roomNumber;
    private int roomTypeId;
    private BigDecimal pricePerNight;
    private RoomStatus status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private String description;
    private String roomTypeName;

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }
}