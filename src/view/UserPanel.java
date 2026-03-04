package view;

import model.entities.User;
import model.entities.Room;
import model.entities.Booking;
import model.entities.Invoice;
import model.service.RoomService;
import model.service.BookingService;
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
    private Scanner scanner;

    public UserPanel(User user) {
        this.loggedInUser = user;
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
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
                    "5. Logout");

            System.out.print("Select an option (1-5): ");
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

        // Show available rooms first
        List<Room> availableRooms = roomService.getRoomsByStatus("AVAILABLE", 1);
        if (availableRooms.isEmpty()) {
            UiUtils.printMessage("No rooms available for booking.");
            return;
        }

        // Display available rooms
        Table roomTable = UiUtils.createDataTable(5);
        roomTable.addCell("ID");
        roomTable.addCell("Room No");
        roomTable.addCell("Type");
        roomTable.addCell("Price/Night");
        roomTable.addCell("Features");

        List<Room> allAvailableRooms = roomService.getRoomsByStatus("AVAILABLE", 1);
        for (Room room : allAvailableRooms) {
            roomTable.addCell(String.valueOf(room.getId()));
            roomTable.addCell(room.getRoomNumber());
            roomTable.addCell(room.getRoomTypeName());
            roomTable.addCell("$" + room.getPricePerNight());
            String features = room.getDescription();
            if (features == null || features.isEmpty()) features = "N/A";
            else if (features.length() > 25) features = features.substring(0, 22) + "...";
            roomTable.addCell(features);
        }
        System.out.println(roomTable.render());

        // Get room ID
        System.out.print("\nEnter Room ID to book: ");
        String roomIdStr = scanner.nextLine().trim();
        int roomId;
        try {
            roomId = Integer.parseInt(roomIdStr);
        } catch (NumberFormatException e) {
            UiUtils.printError("Invalid room ID.");
            return;
        }

        // Verify room exists and is available
        Room selectedRoom = roomService.getRoomById(roomId);
        if (selectedRoom == null || selectedRoom.getStatus() != Room.RoomStatus.AVAILABLE) {
            UiUtils.printError("Room not found or not available.");
            return;
        }

        // Get dates
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
            booking.setRoomId(roomId);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setUsername(loggedInUser.getUsername());
            booking.setUserEmail(loggedInUser.getEmail());
            booking.setUserPhone(loggedInUser.getPhoneNumber());

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
                    "6. Back to login");

            System.out.print("Select an option (1-6): ");
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
                    return; // back to login menu
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    // All remaining methods unchanged (they only use RoomService)
    private void filterByRoomType() throws SQLException {
        UiUtils.printHeader("Available Room Types");
        UiUtils.printMenu(null,
                "1. Regular",
                "2. Family",
                "3. Suite",
                "4. Deluxe");

        System.out.print("Select room type (1-4): ");
        String choice = scanner.nextLine().trim();
        String roomType = null;
        switch (choice) {
            case "1": roomType = "Regular"; break;
            case "2": roomType = "Family"; break;
            case "3": roomType = "Suite"; break;
            case "4": roomType = "Deluxe"; break;
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
                "2. Occupied",
                "3. Maintenance");

        System.out.print("Select status (1-3): ");
        String choice = scanner.nextLine().trim();
        String status = null;
        switch (choice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "OCCUPIED"; break;
            case "3": status = "MAINTENANCE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }
        displayRoomsPaginated(null, status);
    }

    private void filterByTypeAndStatus() throws SQLException {
        UiUtils.printHeader("Select Room Type");
        UiUtils.printMenu(null,
                "1. Regular",
                "2. Family",
                "3. Suite",
                "4. Deluxe");

        System.out.print("Select room type (1-4): ");
        String typeChoice = scanner.nextLine().trim();
        String roomType = null;
        switch (typeChoice) {
            case "1": roomType = "Regular"; break;
            case "2": roomType = "Family"; break;
            case "3": roomType = "Suite"; break;
            case "4": roomType = "Deluxe"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }

        UiUtils.printHeader("Select Room Status");
        UiUtils.printMenu(null,
                "1. Available",
                "2. Occupied",
                "3. Maintenance");

        System.out.print("Select status (1-3): ");
        String statusChoice = scanner.nextLine().trim();
        String status = null;
        switch (statusChoice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "OCCUPIED"; break;
            case "3": status = "MAINTENANCE"; break;
            default:
                UiUtils.printError("Invalid option.");
                return;
        }
        displayRoomsPaginatedWithBoth(roomType, status);
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
                Table table = UiUtils.createDataTable(6);
                table.addCell("ID");
                table.addCell("Room No");
                table.addCell("Type");
                table.addCell("Price/Night");
                table.addCell("Status");
                table.addCell("Features");

                for (Room room : rooms) {
                    table.addCell(String.valueOf(room.getId()));
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

    private void displayRoomsPaginatedWithBoth(String roomType, String status) throws SQLException {
        int pageNumber = 1;
        int rowsPerPage = 5;
        long totalCount = roomService.getRoomCountByType(roomType);
        int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

        while (true) {
            List<Room> rooms = roomService.getRoomsByTypeAndStatus(roomType, status, pageNumber);

            UiUtils.printHeader("FILTERED ROOMS - " + roomType + " - " + status + " (Page " + pageNumber + "/" + totalPages + ")");

            if (rooms.isEmpty()) {
                UiUtils.printMessage("No rooms found matching your criteria.");
            } else {
                Table table = UiUtils.createDataTable(6);
                table.addCell("ID");
                table.addCell("Room No");
                table.addCell("Type");
                table.addCell("Price/Night");
                table.addCell("Status");
                table.addCell("Features");

                for (Room room : rooms) {
                    table.addCell(String.valueOf(room.getId()));
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
                case "p": if (pageNumber > 1) pageNumber--; break;
                case "n": if (pageNumber < totalPages) pageNumber++; break;
                case "b": return;
                default:
                    UiUtils.printError("Invalid option.");
            }
        }
    }

    private void viewDetailedRoomFeatures() throws SQLException {
        UiUtils.printHeader("ROOM DETAILS WITH COMPLETE FEATURES");

        List<Room> rooms = roomService.getAllRoomsPaginated(1);
        Table listTable = UiUtils.createDataTable(5);
        listTable.addCell("ID");
        listTable.addCell("Room No");
        listTable.addCell("Type");
        listTable.addCell("Price/Night");
        listTable.addCell("Status");

        for (Room room : rooms) {
            listTable.addCell(String.valueOf(room.getId()));
            listTable.addCell(room.getRoomNumber());
            listTable.addCell(room.getRoomTypeName());
            listTable.addCell("$" + room.getPricePerNight());
            listTable.addCell(String.valueOf(room.getStatus()));
        }
        System.out.println(listTable.render());

        System.out.print("\nEnter Room ID to view complete features: ");
        String roomIdStr = scanner.nextLine().trim();

        try {
            int roomId = Integer.parseInt(roomIdStr);
            Room room = roomService.getRoomById(roomId);

            if (room == null) {
                UiUtils.printError("Room not found.");
                return;
            }

            UiUtils.printHeader("COMPLETE ROOM DETAILS");

            Table detailTable = UiUtils.createDataTable(2);
            detailTable.addCell("Room ID:");
            detailTable.addCell(String.valueOf(room.getId()));
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

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();

        } catch (NumberFormatException e) {
            UiUtils.printError("Invalid room ID format.");
        }
    }
}
