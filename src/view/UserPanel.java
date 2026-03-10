package view;

import model.entities.User;
import model.entities.Room;
import model.entities.Booking;
import model.entities.Invoice;
import model.service.RoomService;
import model.service.BookingService;
import model.service.UserService;
import model.service.TelegramService;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class UserPanel {
    private User loggedInUser;
    private RoomService roomService;
    private BookingService bookingService;
    private UserService userService;
    private Scanner scanner;

    public UserPanel(User user) {
        this.loggedInUser = user;
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    // Entry point – main menu
    public void start() throws SQLException {
        while (true) {
            UiUtils.printHeader("USER PANEL - Welcome, " + loggedInUser.getUsername());

            UiUtils.printMenu(null,
                    "1. View Rooms",
                    "2. Make a Booking",
                    "3. My Bookings",
                    "4. My Invoices",
                    "5. Telegram Settings",
                    "6. Logout");

            System.out.print("Select an option (1-6): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAvailableRooms();
                    break;
                case "2":
                    makeBooking();
                    break;
                case "3":
                    viewMyBookings();
                    break;
                case "4":
                    viewMyInvoices();
                    break;
                case "5":
                    telegramSettings();
                    break;
                case "6":
                    UiUtils.printMessage("Logging out...");
                    return;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    // ============================================
    // MAKE BOOKING
    // ============================================
    private void makeBooking() throws SQLException {
        UiUtils.printHeader("MAKE A BOOKING");

        // Get dates first
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        System.out.print("Check-in date (yyyy-MM-dd): ");
        String checkInStr = scanner.nextLine().trim();
        LocalDate checkInDate;
        try {
            checkInDate = LocalDate.parse(checkInStr, formatter);
        } catch (DateTimeParseException e) {
            UiUtils.printError("Invalid date format. Use yyyy-MM-dd");
            return;
        }

        System.out.print("Check-out date (yyyy-MM-dd): ");
        String checkOutStr = scanner.nextLine().trim();
        LocalDate checkOutDate;
        try {
            checkOutDate = LocalDate.parse(checkOutStr, formatter);
        } catch (DateTimeParseException e) {
            UiUtils.printError("Invalid date format. Use yyyy-MM-dd");
            return;
        }

        // Validate dates
        if (!checkOutDate.isAfter(checkInDate)) {
            UiUtils.printError("Check-out date must be after check-in date.");
            return;
        }

        if (checkInDate.isBefore(LocalDate.now())) {
            UiUtils.printError("Check-in date cannot be in the past.");
            return;
        }

        // Show available rooms for these dates
        List<Room> availableRooms = roomService.getAvailableRoomsByDate(checkInDate, checkOutDate, 1);
        if (availableRooms.isEmpty()) {
            UiUtils.printMessage("No rooms available for the selected dates.");
            return;
        }

        // Show available room numbers
        System.out.println("\nAvailable rooms: " + availableRooms.stream()
                .map(Room::getRoomNumber)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None"));

        // Get room number
        System.out.print("\nEnter Room Number to book (or press Enter to cancel): ");
        String roomNumber = scanner.nextLine().trim();
        if (roomNumber.isEmpty()) return;

        // Verify room exists and is available for these dates
        Room selectedRoom = roomService.getRoomByNumber(roomNumber);
        if (selectedRoom == null) {
            UiUtils.printError("Room not found.");
            return;
        }

        // Double check availability (to prevent race conditions or invalid selection)
        boolean isAvailable = availableRooms.stream().anyMatch(r -> r.getRoomNumber().equals(roomNumber));
        if (!isAvailable) {
            UiUtils.printError("Selected room is not available for these dates.");
            return;
        }

        // Calculate and show price
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        java.math.BigDecimal basePrice = selectedRoom.getPricePerNight().multiply(java.math.BigDecimal.valueOf(nights));
        java.math.BigDecimal discount = basePrice.multiply(java.math.BigDecimal.valueOf(0.30));
        java.math.BigDecimal totalPrice = basePrice.subtract(discount);

        // Show booking summary
        Table summaryTable = UiUtils.createDataTable(2);
        summaryTable.addCell("BOOKING SUMMARY");
        summaryTable.addCell("");
        summaryTable.addCell("Room:");
        summaryTable.addCell(selectedRoom.getRoomNumber() + " (" + selectedRoom.getRoomTypeName() + ")");
        summaryTable.addCell("Check-in:");
        summaryTable.addCell(checkInDate.toString());
        summaryTable.addCell("Check-out:");
        summaryTable.addCell(checkOutDate.toString());
        summaryTable.addCell("Nights:");
        summaryTable.addCell(String.valueOf(nights));
        summaryTable.addCell("Price per night:");
        summaryTable.addCell("$" + selectedRoom.getPricePerNight());
        summaryTable.addCell("Base price:");
        summaryTable.addCell("$" + basePrice);
        summaryTable.addCell("Discount (30%):");
        summaryTable.addCell("-$" + discount);
        summaryTable.addCell("TOTAL:");
        summaryTable.addCell("$" + totalPrice);
        System.out.println(summaryTable.render());

        // Confirm
        System.out.print("Confirm booking? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y")) {
            UiUtils.printMessage("Booking cancelled.");
            return;
        }

        // Create booking
        try {
            Booking booking = new Booking();
            booking.setUserId(loggedInUser.getId());
            booking.setRoomId(selectedRoom.getId());
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setUsername(loggedInUser.getUsername());
            booking.setUserEmail(loggedInUser.getEmail());
            booking.setUserPhone(loggedInUser.getPhoneNumber());
            booking.setTelegramChatId(loggedInUser.getTelegramChatId());

            Booking createdBooking = bookingService.createBooking(booking);

            UiUtils.printSuccess("BOOKING SUCCESSFUL!");

            Table bookingInfoTable = UiUtils.createDataTable(2);
            bookingInfoTable.addCell("Booking ID:");
            bookingInfoTable.addCell(String.valueOf(createdBooking.getId()));
            bookingInfoTable.addCell("Status:");
            bookingInfoTable.addCell(createdBooking.getStatus().name() + " (Waiting for admin approval)");
            bookingInfoTable.addCell("Total Price:");
            bookingInfoTable.addCell("$" + createdBooking.getTotalPrice());
            System.out.println(bookingInfoTable.render());

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();

        } catch (SQLException e) {
            UiUtils.printError("Error creating booking: " + e.getMessage());
        }
    }

    // ============================================
    // MY BOOKINGS
    // ============================================
    private void viewMyBookings() throws SQLException {
        List<Booking> bookings = bookingService.getUserBookings(loggedInUser.getId());

        UiUtils.printHeader("MY BOOKINGS");

        if (bookings.isEmpty()) {
            UiUtils.printMessage("You have no bookings yet.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Display bookings
        Table table = UiUtils.createDataTable(6);
        table.addCell("ID");
        table.addCell("Room");
        table.addCell("Check-in");
        table.addCell("Check-out");
        table.addCell("Total");
        table.addCell("Status");

        for (Booking booking : bookings) {
            table.addCell(String.valueOf(booking.getId()));
            table.addCell(booking.getRoomNumber());
            table.addCell(booking.getCheckInDate().toString());
            table.addCell(booking.getCheckOutDate().toString());
            table.addCell("$" + booking.getTotalPrice());
            table.addCell(booking.getStatus().name());
        }
        System.out.println(table.render());

        // View details
        System.out.print("\nEnter Booking ID to view details (or press Enter to go back): ");
        String bookingIdStr = scanner.nextLine().trim();

        if (!bookingIdStr.isEmpty()) {
            try {
                int bookingId = Integer.parseInt(bookingIdStr);
                Booking booking = bookings.stream()
                        .filter(b -> b.getId() == bookingId)
                        .findFirst()
                        .orElse(null);

                if (booking != null) {
                    viewBookingDetails(booking);
                } else {
                    UiUtils.printError("Booking not found.");
                }
            } catch (NumberFormatException e) {
                UiUtils.printError("Invalid booking ID.");
            }
        }
    }

    private void viewBookingDetails(Booking booking) throws SQLException {
        Table detailTable = UiUtils.createDataTable(2);
        detailTable.addCell("BOOKING DETAILS");
        detailTable.addCell("");
        detailTable.addCell("Booking ID:");
        detailTable.addCell(String.valueOf(booking.getId()));
        detailTable.addCell("Room:");
        detailTable.addCell(booking.getRoomNumber() + " (" + booking.getRoomTypeName() + ")");
        detailTable.addCell("Check-in:");
        detailTable.addCell(booking.getCheckInDate().toString());
        detailTable.addCell("Check-out:");
        detailTable.addCell(booking.getCheckOutDate().toString());
        detailTable.addCell("Base Price:");
        detailTable.addCell("$" + booking.getBasePrice());
        detailTable.addCell("Discount:");
        detailTable.addCell("-$" + booking.getDiscountAmount());
        detailTable.addCell("Total:");
        detailTable.addCell("$" + booking.getTotalPrice());
        detailTable.addCell("Status:");
        detailTable.addCell(booking.getStatus().name());
        detailTable.addCell("Created:");
        detailTable.addCell(booking.getCreatedAt().toString());
        System.out.println(detailTable.render());

        // Show invoice option
        Invoice invoice = bookingService.getInvoiceByBookingId(booking.getId());
        if (invoice != null) {
            System.out.print("\nView invoice? (y/n): ");
            String viewInvoice = scanner.nextLine().trim().toLowerCase();
            if (viewInvoice.equals("y")) {
                viewInvoiceDetails(invoice);
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ============================================
    // MY INVOICES
    // ============================================
    private void viewMyInvoices() throws SQLException {
        List<Invoice> invoices = bookingService.getUserInvoices(loggedInUser.getId());

        UiUtils.printHeader("MY INVOICES");

        if (invoices.isEmpty()) {
            UiUtils.printMessage("You have no invoices yet.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Display invoices
        Table table = UiUtils.createDataTable(5);
        table.addCell("Invoice No");
        table.addCell("Room");
        table.addCell("Check-in");
        table.addCell("Total");
        table.addCell("Status");

        for (Invoice invoice : invoices) {
            table.addCell(invoice.getInvoiceNumber());
            table.addCell(invoice.getRoomNumber());
            table.addCell(invoice.getCheckInDate().toString());
            table.addCell("$" + invoice.getTotalAmount());
            table.addCell(invoice.getInvoiceStatus().name());
        }
        System.out.println(table.render());

        // View details
        System.out.print("\nEnter Invoice Number to view details (or press Enter to go back): ");
        String invoiceNum = scanner.nextLine().trim();

        if (!invoiceNum.isEmpty()) {
            Invoice invoice = invoices.stream()
                    .filter(i -> i.getInvoiceNumber().equalsIgnoreCase(invoiceNum))
                    .findFirst()
                    .orElse(null);

            if (invoice != null) {
                viewInvoiceDetails(invoice);
            } else {
                UiUtils.printError("Invoice not found.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void viewInvoiceDetails(Invoice invoice) {
        Table detailTable = UiUtils.createDataTable(2);
        detailTable.addCell("INVOICE");
        detailTable.addCell("");
        detailTable.addCell("Invoice Number:");
        detailTable.addCell(invoice.getInvoiceNumber());
        detailTable.addCell("Issue Date:");
        detailTable.addCell(invoice.getIssueDate().toString());
        detailTable.addCell("Guest Name:");
        detailTable.addCell(invoice.getGuestName());
        detailTable.addCell("Guest Email:");
        detailTable.addCell(invoice.getGuestEmail() != null ? invoice.getGuestEmail() : "N/A");
        detailTable.addCell("Guest Phone:");
        detailTable.addCell(invoice.getGuestPhone() != null ? invoice.getGuestPhone() : "N/A");
        detailTable.addCell("Room:");
        detailTable.addCell(invoice.getRoomNumber() + " (" + invoice.getRoomTypeName() + ")");
        detailTable.addCell("Check-in:");
        detailTable.addCell(invoice.getCheckInDate().toString());
        detailTable.addCell("Check-out:");
        detailTable.addCell(invoice.getCheckOutDate().toString());
        detailTable.addCell("Nights:");
        detailTable.addCell(String.valueOf(invoice.getNights()));
        detailTable.addCell("Price per night:");
        detailTable.addCell("$" + invoice.getPricePerNight());
        detailTable.addCell("Discount:");
        detailTable.addCell("-$" + invoice.getDiscountAmount());
        detailTable.addCell("TOTAL AMOUNT:");
        detailTable.addCell("$" + invoice.getTotalAmount());
        detailTable.addCell("Payment Status:");
        detailTable.addCell(invoice.getInvoiceStatus().name());
        System.out.println(detailTable.render());
    }

    // ============================================
    // VIEW ROOMS (existing functionality)
    // ============================================
    private void viewAvailableRooms() throws SQLException {
        while (true) {
            UiUtils.printHeader("VIEW ROOMS");

            UiUtils.printMenu(null,
                    "1. View all rooms (with features)",
                    "2. Filter by room type",
                    "3. Filter by availability status",
                    "4. Filter by type AND status",
                    "5. View detailed room features",
                    "6. SEARCH AVAILABLE BY DATE",
                    "7. Back to Menu");

            System.out.print("Select an option (1-7): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayRoomsPaginated(null, null);
                    break;
                case "2":
                    filterByRoomType();
                    break;
                case "3":
                    filterByRoomStatus();
                    break;
                case "4":
                    filterByTypeAndStatus();
                    break;
                case "5":
                    viewDetailedRoomFeatures();
                    break;
                case "6":
                    searchByDate();
                    break;
                case "7":
                    return; // back to login menu
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    private void searchByDate() throws SQLException {
        UiUtils.printHeader("SEARCH BY DATE");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        System.out.print("Check-in date (yyyy-MM-dd): ");
        String checkInStr = scanner.nextLine().trim();
        LocalDate checkInDate;
        try {
            checkInDate = LocalDate.parse(checkInStr, formatter);
        } catch (DateTimeParseException e) {
            UiUtils.printError("Invalid date format.");
            return;
        }

        System.out.print("Check-out date (yyyy-MM-dd): ");
        String checkOutStr = scanner.nextLine().trim();
        LocalDate checkOutDate;
        try {
            checkOutDate = LocalDate.parse(checkOutStr, formatter);
        } catch (DateTimeParseException e) {
            UiUtils.printError("Invalid date format.");
            return;
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            UiUtils.printError("Check-out date must be after check-in date.");
            return;
        }

        displayAvailableRoomsByDatePaginated(checkInDate, checkOutDate);
    }

    private void displayAvailableRoomsByDatePaginated(LocalDate checkIn, LocalDate checkOut) throws SQLException {
        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            List<Room> rooms = roomService.getAvailableRoomsByDate(checkIn, checkOut, pageNumber);
            long totalCount = roomService.getAvailableRoomCountByDate(checkIn, checkOut);
            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

            UiUtils.printHeader("AVAILABLE ROOMS [" + checkIn + " to " + checkOut + "] (Page " + pageNumber + "/" + totalPages + ")");

            if (rooms.isEmpty()) {
                UiUtils.printMessage("No rooms available for the selected dates.");
            } else {
                Table table = UiUtils.createDataTable(5);
                table.addCell("Room No");
                table.addCell("Type");
                table.addCell("Price/Night");
                table.addCell("Current Status");
                table.addCell("Features");

                for (Room room : rooms) {
                    table.addCell(room.getRoomNumber());
                    table.addCell(room.getRoomTypeName());
                    table.addCell("$" + room.getPricePerNight());
                    table.addCell(String.valueOf(room.getStatus()));

                    String features = room.getDescription();
                    if (features == null || features.isEmpty()) {
                        features = "No features listed";
                    } else if (features.length() > 30) {
                        features = features.substring(0, 27) + "...";
                    }
                    table.addCell(features);
                }
                System.out.println(table.render());
            }

            // Navigation (simplified for brevity)
            if (totalPages > 1) {
                UiUtils.printMenu(null, "p - Previous", "n - Next", "b - Back");
            } else {
                UiUtils.printMenu(null, "b - Back");
            }

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();
            if (navChoice.equals("p") && pageNumber > 1) pageNumber--;
            else if (navChoice.equals("n") && pageNumber < totalPages) pageNumber++;
            else if (navChoice.equals("b")) return;
        }
    }

    // All remaining methods unchanged (they only use RoomService)
    private void filterByRoomType() throws SQLException {
        UiUtils.printHeader("Available Room Types");
        UiUtils.printMenu(null,
                "1. SINGLE",
                "2. DOUBLE",
                "3. SUITE",
                "4. DELUXE",
                "5. PENTHOUSE");

        System.out.print("Select room type (1-5): ");
        String choice = scanner.nextLine().trim();
        String roomType = null;
        switch (choice) {
            case "1": roomType = "SINGLE"; break;
            case "2": roomType = "DOUBLE"; break;
            case "3": roomType = "SUITE"; break;
            case "4": roomType = "DELUXE"; break;
            case "5": roomType = "PENTHOUSE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }
        displayRoomsPaginated(roomType, null);
    }

    private void filterByRoomStatus() throws SQLException {
        UiUtils.printHeader("Room Status Options");
        UiUtils.printMenu(null,
                "1. Available",
                "2. Maintenance");

        System.out.print("Select status (1-2): ");
        String choice = scanner.nextLine().trim();
        String status = null;
        switch (choice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "MAINTENANCE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }
        displayRoomsPaginated(null, status);
    }

    private void filterByTypeAndStatus() throws SQLException {
        UiUtils.printHeader("Filter by Type AND Status");

        // Select room type
        UiUtils.printMenu("Select Room Type",
                "1. SINGLE",
                "2. DOUBLE",
                "3. SUITE",
                "4. DELUXE",
                "5. PENTHOUSE");

        System.out.print("Select room type (1-5): ");
        String typeChoice = scanner.nextLine().trim();
        String roomType = null;
        switch (typeChoice) {
            case "1": roomType = "SINGLE"; break;
            case "2": roomType = "DOUBLE"; break;
            case "3": roomType = "SUITE"; break;
            case "4": roomType = "DELUXE"; break;
            case "5": roomType = "PENTHOUSE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }

        // Select status
        UiUtils.printMenu("Select Room Status",
                "1. Available",
                "2. Maintenance");

        System.out.print("Select status (1-2): ");
        String statusChoice = scanner.nextLine().trim();
        String status = null;
        switch (statusChoice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "MAINTENANCE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }
        displayRoomsFiltered(roomType, status);
    }

    private void displayRoomsFiltered(String roomType, String status) throws SQLException {
        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            long totalCount = roomService.getRoomCountByTypeAndStatus(roomType, status);
            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);
            if (totalPages == 0) totalPages = 1;

            List<Room> rooms = roomService.getRoomsByTypeAndStatus(roomType, status, pageNumber);

            UiUtils.printHeader("FILTERED ROOMS - " + roomType + " - " + status + " (Page " + pageNumber + "/" + totalPages + ")");

            if (rooms.isEmpty()) {
                UiUtils.printMessage("No rooms found matching your criteria.");
            } else {
                Table table = UiUtils.createDataTable(4);
                table.addCell("Room No");
                table.addCell("Type");
                table.addCell("Price/Night");
                table.addCell("Status");

                for (Room room : rooms) {
                    table.addCell(room.getRoomNumber());
                    table.addCell(room.getRoomTypeName());
                    table.addCell("$" + room.getPricePerNight());
                    table.addCell(String.valueOf(room.getStatus()));
                }
                System.out.println(table.render());
            }

            // Navigation
            if (pageNumber > 1 || pageNumber < totalPages) {
                String prev = pageNumber > 1 ? "p - Previous page" : "";
                String next = pageNumber < totalPages ? "n - Next page" : "";
                if (!prev.isEmpty() && !next.isEmpty()) {
                    UiUtils.printMenu(null, prev, next, "b - Back", "v - View room details");
                } else if (!prev.isEmpty()) {
                    UiUtils.printMenu(null, prev, "b - Back", "v - View room details");
                } else {
                    UiUtils.printMenu(null, next, "b - Back", "v - View room details");
                }
            } else {
                UiUtils.printMenu(null, "b - Back", "v - View room details");
            }

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p": if (pageNumber > 1) pageNumber--; break;
                case "n": if (pageNumber < totalPages) pageNumber++; break;
                case "b": return;
                case "v":
                    viewRoomDetailsByInput();
                    break;
                default:
                    UiUtils.printError("Invalid option.");
            }
        }
    }

    private void viewRoomDetailsByInput() throws SQLException {
        System.out.print("\nEnter Room Number to view details: ");
        String roomNum = scanner.nextLine().trim();

        if (roomNum.isEmpty()) return;

        Room room = roomService.getRoomByNumber(roomNum);
        if (room == null) {
            UiUtils.printError("Room not found.");
            return;
        }

        UiUtils.printHeader("ROOM DETAILS");

        Table detailTable = UiUtils.createDataTable(2);
        detailTable.addCell("Room Number:");
        detailTable.addCell(room.getRoomNumber());
        detailTable.addCell("Room Type:");
        detailTable.addCell(room.getRoomTypeName());
        detailTable.addCell("Price per Night:");
        detailTable.addCell("$" + room.getPricePerNight());
        detailTable.addCell("Status:");
        detailTable.addCell(String.valueOf(room.getStatus()));
        detailTable.addCell("Features:");
        detailTable.addCell(room.getDescription() != null ? room.getDescription() : "No features listed");
        System.out.println(detailTable.render());
    }

    private void displayRoomsPaginated(String roomType, String status) throws SQLException {
        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            List<Room> rooms;
            long totalCount;

            if (roomType != null && status == null) {
                rooms = roomService.getRoomsByType(roomType, pageNumber);
                totalCount = roomService.getRoomCountByType(roomType);
            } else if (roomType == null && status != null) {
                rooms = roomService.getRoomsByStatus(status, pageNumber);
                totalCount = roomService.getRoomCountByStatus(status);
            } else {
                rooms = roomService.getAllRoomsPaginated(pageNumber);
                totalCount = roomService.getTotalRoomsCount();
            }

            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

            UiUtils.printHeader("AVAILABLE ROOMS (Page " + pageNumber + "/" + totalPages + ")");

            if (rooms.isEmpty()) {
                UiUtils.printMessage("No rooms found.");
            } else {
                Table table = UiUtils.createDataTable(5);
                table.addCell("Room No");
                table.addCell("Type");
                table.addCell("Price/Night");
                table.addCell("Status");
                table.addCell("Features");

                for (Room room : rooms) {
                    table.addCell(room.getRoomNumber());
                    table.addCell(room.getRoomTypeName());
                    table.addCell("$" + room.getPricePerNight());
                    table.addCell(String.valueOf(room.getStatus()));

                    String features = room.getDescription();
                    if (features == null || features.isEmpty()) {
                        features = "No features listed";
                    } else if (features.length() > 30) {
                        features = features.substring(0, 27) + "...";
                    }
                    table.addCell(features);
                }
                System.out.println(table.render());
            }

            // Navigation
            if (pageNumber > 1 || pageNumber < totalPages) {
                String prev = pageNumber > 1 ? "p - Previous page" : "";
                String next = pageNumber < totalPages ? "n - Next page" : "";
                if (!prev.isEmpty() && !next.isEmpty()) {
                    UiUtils.printMenu(null, prev, next, "b - Back");
                } else if (!prev.isEmpty()) {
                    UiUtils.printMenu(null, prev, "b - Back");
                } else {
                    UiUtils.printMenu(null, next, "b - Back");
                }
            } else {
                UiUtils.printMenu(null, "b - Back");
            }

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p":
                    if (pageNumber > 1) pageNumber--;
                    else UiUtils.printError("Already on first page.");
                    break;
                case "n":
                    if (pageNumber < totalPages) pageNumber++;
                    else UiUtils.printError("Already on last page.");
                    break;
                case "b":
                    return;
                default:
                    UiUtils.printError("Invalid option.");
            }
        }
    }

    private void viewDetailedRoomFeatures() throws SQLException {
        viewRoomDetailsByInput();
    }

    private void telegramSettings() {
        UiUtils.printHeader("TELEGRAM SETTINGS");

        Long currentChatId = loggedInUser.getTelegramChatId();
        if (currentChatId != null) {
            System.out.println("Currently connected Telegram Chat ID: " + currentChatId);
        } else {
            System.out.println("Telegram is not connected.");
        }

        System.out.println("\nTo connect your Telegram:");
        System.out.println("1. Find your Chat ID (you can use @userinfobot on Telegram)");
        System.out.println("2. Enter your Chat ID below to receive booking notifications.");

        System.out.print("\nEnter your Telegram Chat ID (or press Enter to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) return;

        try {
            long chatId = Long.parseLong(input);
            boolean success = userService.updateTelegramChatId(loggedInUser.getId(), chatId);
            if (success) {
                loggedInUser.setTelegramChatId(chatId);
                UiUtils.printSuccess("Telegram Chat ID updated successfully!");
                
                // Send a test message
                TelegramService telegramService = new TelegramService();
                telegramService.sendMessage(chatId, "<b>Success!</b> Your account is now linked to our Hotel Reservation System. You will receive notifications here.");
            } else {
                UiUtils.printError("Failed to update Telegram Chat ID.");
            }
        } catch (NumberFormatException e) {
            UiUtils.printError("Invalid Chat ID. Please enter a numeric value.");
        } catch (SQLException e) {
            UiUtils.printError("Database error: " + e.getMessage());
        }
    }
}
