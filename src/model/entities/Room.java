package model.entities;

import java.math.BigDecimal;

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

    public Room() {
    }

    public Room(int id, String roomNumber, int roomTypeId, BigDecimal pricePerNight, RoomStatus status, String description, String roomTypeName) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.pricePerNight = pricePerNight;
        this.status = status;
        this.description = description;
        this.roomTypeName = roomTypeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomTypeId=" + roomTypeId +
                ", pricePerNight=" + pricePerNight +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", roomTypeName='" + roomTypeName + '\'' +
                '}';
    }
}
