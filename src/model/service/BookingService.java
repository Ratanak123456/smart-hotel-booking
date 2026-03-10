package model.service;

import model.dao.BookingDao;
import model.dao.InvoiceDao;
import model.dao.RoomDao;
import model.entities.Booking;
import model.entities.Invoice;
import model.entities.Room;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class BookingService {
    private BookingDao bookingDao = new BookingDao();
    private InvoiceDao invoiceDao = new InvoiceDao();
    private RoomDao roomDao = new RoomDao();
    private TelegramService telegramService = new TelegramService();
    private static final int PAGE_SIZE = 5;

    // Create a new booking (NO invoice yet - invoice created upon approval)
    public Booking createBooking(Booking booking) throws SQLException {
        // Get room details
        Room room = roomDao.getRoomById(booking.getRoomId());
        if (room == null) {
            throw new SQLException("Room not found");
        }

        // Calculate price
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (nights <= 0) {
            throw new SQLException("Check-out date must be after check-in date");
        }

        BigDecimal basePrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal discountAmount = basePrice.multiply(BigDecimal.valueOf(0.30)); // 30% discount
        BigDecimal totalPrice = basePrice.subtract(discountAmount);

        booking.setBasePrice(basePrice);
        booking.setDiscountAmount(discountAmount);
        booking.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        booking.setStatus(Booking.BookingStatus.PENDING);

        // Insert booking (no invoice yet)
        int bookingId = bookingDao.insert(booking);
        if (bookingId == -1) {
            throw new SQLException("Failed to create booking");
        }

        booking.setId(bookingId);
        booking.setRoomNumber(room.getRoomNumber());
        booking.setRoomTypeName(room.getRoomTypeName());

        // Notify user about pending booking
        if (booking.getTelegramChatId() != null) {
            telegramService.notifyBookingStatus(booking.getTelegramChatId(), String.valueOf(booking.getId()), "PENDING", room.getRoomNumber());
        }

        return booking;
    }

    // Generate invoice when admin approves
    public Invoice generateInvoiceOnApproval(Booking booking) throws SQLException {
        Room room = roomDao.getRoomById(booking.getRoomId());
        if (room == null) {
            throw new SQLException("Room not found");
        }

        Invoice invoice = generateInvoice(booking, room);
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.PAID); // Auto-mark as paid (no payment needed)
        int invoiceId = invoiceDao.insert(invoice);
        invoice.setId(invoiceId);

        return invoice;
    }

    private Invoice generateInvoice(Booking booking, Room room) {
        Invoice invoice = new Invoice();
        invoice.setBookingId(booking.getId());
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setIssueDate(LocalDate.now());
        invoice.setGuestName(booking.getUsername());
        invoice.setGuestEmail(booking.getUserEmail());
        invoice.setGuestPhone(booking.getUserPhone());
        invoice.setRoomNumber(room.getRoomNumber());
        invoice.setRoomTypeName(room.getRoomTypeName());
        invoice.setCheckInDate(booking.getCheckInDate());
        invoice.setCheckOutDate(booking.getCheckOutDate());
        invoice.setNights((int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate()));
        invoice.setPricePerNight(room.getPricePerNight());
        invoice.setDiscountAmount(booking.getDiscountAmount());
        invoice.setTotalAmount(booking.getTotalPrice());
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.PENDING);

        return invoice;
    }

    public List<Booking> getUserBookings(int userId) throws SQLException {
        return bookingDao.getBookingsByUserId(userId);
    }

    public List<Booking> getUserBookingsPaginated(int userId, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return bookingDao.getBookingsByUserIdPaginated(userId, PAGE_SIZE, offset);
    }

    public long getUserBookingCount(int userId) throws SQLException {
        return bookingDao.getBookingCountByUserId(userId);
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        return bookingDao.getBookingById(bookingId);
    }

    public Invoice getInvoiceByBookingId(int bookingId) throws SQLException {
        return invoiceDao.getInvoiceByBookingId(bookingId);
    }

    public List<Invoice> getUserInvoices(int userId) throws SQLException {
        return invoiceDao.getInvoicesByUserId(userId);
    }

    public List<Invoice> getUserInvoicesPaginated(int userId, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return invoiceDao.getInvoicesByUserIdPaginated(userId, PAGE_SIZE, offset);
    }

    public long getUserInvoiceCount(int userId) throws SQLException {
        return invoiceDao.getInvoiceCountByUserId(userId);
    }

    // Admin functions
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDao.getAllBookings();
    }

    public List<Booking> getPendingBookings() throws SQLException {
        return bookingDao.getBookingsByStatus("PENDING");
    }

    public List<Booking> getPendingBookingsPaginated(int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return bookingDao.getBookingsByStatusPaginated("PENDING", PAGE_SIZE, offset);
    }

    public long getPendingBookingCount() throws SQLException {
        return bookingDao.getBookingCountByStatus("PENDING");
    }

    public boolean approveBooking(int bookingId) throws SQLException {
        Booking booking = bookingDao.getBookingById(bookingId);
        if (booking == null) {
            return false;
        }

        // Update booking status to ACTIVE
        boolean updated = bookingDao.updateStatus(bookingId, "ACTIVE");
        if (updated) {
            // Generate invoice on approval
            Invoice invoice = generateInvoiceOnApproval(booking);

            // Notify user
            if (booking.getTelegramChatId() != null) {
                telegramService.notifyBookingStatus(booking.getTelegramChatId(), String.valueOf(booking.getId()), "ACTIVE", booking.getRoomNumber());
                telegramService.notifyInvoice(booking.getTelegramChatId(), invoice);
            }
        }
        return updated;
    }

    public boolean rejectBooking(int bookingId) throws SQLException {
        Booking booking = bookingDao.getBookingById(bookingId);
        if (booking == null) {
            return false;
        }

        // Update booking status to CANCELLED
        boolean updated = bookingDao.updateStatus(bookingId, "CANCELLED");
        if (updated) {
            // Notify user
            if (booking.getTelegramChatId() != null) {
                telegramService.notifyBookingStatus(booking.getTelegramChatId(), String.valueOf(booking.getId()), "CANCELLED", booking.getRoomNumber());
            }
        }
        return updated;
    }

    public int getTotalPages(long totalCount) {
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }
}
