package model.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

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

    public Invoice() {
    }

    public Invoice(int id, int bookingId, String invoiceNumber, LocalDate issueDate, String guestName, String guestEmail, String guestPhone, String roomNumber, String roomTypeName, LocalDate checkInDate, LocalDate checkOutDate, int nights, BigDecimal pricePerNight, BigDecimal discountAmount, BigDecimal totalAmount, InvoiceStatus invoiceStatus, Timestamp createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.roomNumber = roomNumber;
        this.roomTypeName = roomTypeName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.pricePerNight = pricePerNight;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.invoiceStatus = invoiceStatus;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
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

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", issueDate=" + issueDate +
                ", guestName='" + guestName + '\'' +
                ", guestEmail='" + guestEmail + '\'' +
                ", guestPhone='" + guestPhone + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomTypeName='" + roomTypeName + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", nights=" + nights +
                ", pricePerNight=" + pricePerNight +
                ", discountAmount=" + discountAmount +
                ", totalAmount=" + totalAmount +
                ", invoiceStatus=" + invoiceStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
