package model.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

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

    public Booking() {
    }

    public Booking(int id, int userId, int roomId, LocalDate checkInDate, LocalDate checkOutDate, BigDecimal basePrice, BigDecimal discountAmount, BigDecimal totalPrice, BookingStatus status, Long telegramChatId, Timestamp createdAt, String roomNumber, String roomTypeName, String username, String userEmail, String userPhone) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.basePrice = basePrice;
        this.discountAmount = discountAmount;
        this.totalPrice = totalPrice;
        this.status = status;
        this.telegramChatId = telegramChatId;
        this.createdAt = createdAt;
        this.roomNumber = roomNumber;
        this.roomTypeName = roomTypeName;
        this.username = username;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", userId=" + userId +
                ", roomId=" + roomId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", basePrice=" + basePrice +
                ", discountAmount=" + discountAmount +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", telegramChatId=" + telegramChatId +
                ", createdAt=" + createdAt +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomTypeName='" + roomTypeName + '\'' +
                ", username='" + username + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPhone='" + userPhone + '\'' +
                '}';
    }
}
