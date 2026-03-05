package view;

import model.entities.User;
import model.entities.Room;
import model.entities.Booking;
import model.service.RoomService;
import model.service.UserService;
import model.service.BookingService;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private User loggedInAdmin;
    private RoomService roomService;
    private UserService userService;
    private BookingService bookingService;
    private Scanner scanner;

    public AdminPanel(User admin) {
        this.loggedInAdmin = admin;
        this.roomService = new RoomService();
        this.userService = new UserService();
        this.bookingService = new BookingService();
        this.scanner = new Scanner(System.in);
    }

    public void start() throws SQLException {
        while (true) {
            UiUtils.printHeader("ADMIN PANEL - Welcome, " + loggedInAdmin.getUsername());

            UiUtils.printMenu(null,
                    "1. Room Management",
                    "2. User Management",
                    "3. Booking Management",
                    "4. Logout");

            System.out.print("Select an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    roomManagement();
                    break;
                case "2":
                    userManagement();
                    break;
                case "3":
                    bookingManagement();
                    break;
                case "4":
                    UiUtils.printMessage("Logged out successfully.");
                    return;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    // ==================== ROOM MANAGEMENT ====================
    private void roomManagement() throws SQLException {
        while (true) {
            UiUtils.printHeader("ROOM MANAGEMENT");

            UiUtils.printMenu(null,
                    "1. View all rooms",
                    "2. Search/Filter by room type",
                    "3. Search/Filter by availability status",
                    "4. Filter by type AND status",
                    "5. Sort rooms by price",
                    "6. View detailed room features",
                    "7. Back to admin menu");

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
                    sortRoomsByPrice();
                    break;
                case "6":
                    viewDetailedRoomFeatures();
                    break;
                case "7":
                    return;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

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

            UiUtils.printHeader("ROOMS (Page " + pageNumber + "/" + totalPages + ")");

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
                // Filter out empty strings
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
                case "p": if (pageNumber > 1) pageNumber--; break;
                case "n": if (pageNumber < totalPages) pageNumber++; break;
                case "b": return;
                default:
                    UiUtils.printError("Invalid option.");
            }
        }
    }

    private void sortRoomsByPrice() throws SQLException {
        UiUtils.printMenu("Sort Options",
                "1. Sort by price (Low to High)",
                "2. Sort by price (High to Low)");

        System.out.print("Select sort option (1-2): ");
        String choice = scanner.nextLine().trim();

        final boolean ascending;
        if (choice.equals("2")) {
            ascending = false;
        } else if (!choice.equals("1")) {
            UiUtils.printError("Invalid option.");
            return;
        } else {
            ascending = true;
        }

        int pageNumber = 1;
        int rowsPerPage = 5;
        long totalCount = roomService.getTotalRoomsCount();
        int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

        while (true) {
            List<Room> rooms = roomService.getAllRoomsPaginated(pageNumber);

            // Sort manually
            rooms.sort((r1, r2) -> {
                if (ascending) {
                    return r1.getPricePerNight().compareTo(r2.getPricePerNight());
                } else {
                    return r2.getPricePerNight().compareTo(r1.getPricePerNight());
                }
            });

            String sortLabel = ascending ? "Low to High" : "High to Low";
            UiUtils.printHeader("ROOMS SORTED BY PRICE (" + sortLabel + ") - Page " + pageNumber + "/" + totalPages);

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
        Table listTable = UiUtils.createDataTable(4);
        listTable.addCell("Room No");
        listTable.addCell("Type");
        listTable.addCell("Price/Night");
        listTable.addCell("Status");

        for (Room room : rooms) {
            listTable.addCell(room.getRoomNumber());
            listTable.addCell(room.getRoomTypeName());
            listTable.addCell("$" + room.getPricePerNight());
            listTable.addCell(String.valueOf(room.getStatus()));
        }
        System.out.println(listTable.render());

        System.out.print("\nEnter Room Number to view complete features: ");
        String roomNum = scanner.nextLine().trim();

        Room room = roomService.getRoomByNumber(roomNum);

        if (room == null) {
            UiUtils.printError("Room not found.");
            return;
        }

        UiUtils.printHeader("COMPLETE ROOM DETAILS");

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

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ==================== USER MANAGEMENT ====================
    private void userManagement() throws SQLException {
        while (true) {
            UiUtils.printHeader("USER MANAGEMENT");

            UiUtils.printMenu(null,
                    "1. List all users",
                    "2. Search user by username",
                    "3. Delete user",
                    "4. Back to admin menu");

            System.out.print("Select an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listAllUsers();
                    break;
                case "2":
                    searchUser();
                    break;
                case "3":
                    deleteUser();
                    break;
                case "4":
                    return;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    private void listAllUsers() throws SQLException {
        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            List<User> users = userService.getAllUsersPaginated(pageNumber);
            int totalCount = userService.getTotalUsersCount();
            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

            UiUtils.printHeader("ALL USERS (Page " + pageNumber + "/" + totalPages + ")");

            if (users.isEmpty()) {
                UiUtils.printMessage("No users found.");
            } else {
                Table table = UiUtils.createDataTable(6);
                table.addCell("ID");
                table.addCell("Username");
                table.addCell("Email");
                table.addCell("Phone");
                table.addCell("Role");
                table.addCell("Status");

                for (User user : users) {
                    table.addCell(String.valueOf(user.getId()));
                    table.addCell(user.getUsername());
                    table.addCell(user.getEmail() != null ? user.getEmail() : "N/A");
                    table.addCell(user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A");
                    table.addCell(user.getRole());
                    table.addCell(user.getDeletedAt() == null ? "Active" : "Deleted");
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

    private void searchUser() throws SQLException {
        System.out.print("Enter username to search: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            UiUtils.printError("Search term cannot be empty.");
            return;
        }

        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            List<User> users = userService.searchUsersByUsername(searchTerm, pageNumber);
            int totalCount = userService.getSearchUsersCount(searchTerm);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / rowsPerPage));

            UiUtils.printHeader("SEARCH RESULTS FOR '" + searchTerm + "' (Page " + pageNumber + "/" + totalPages + ")");

            if (users.isEmpty()) {
                UiUtils.printMessage("No users found matching '" + searchTerm + "'.");
            } else {
                Table table = UiUtils.createDataTable(6);
                table.addCell("ID");
                table.addCell("Username");
                table.addCell("Email");
                table.addCell("Phone");
                table.addCell("Role");
                table.addCell("Status");

                for (User user : users) {
                    table.addCell(String.valueOf(user.getId()));
                    table.addCell(user.getUsername());
                    table.addCell(user.getEmail() != null ? user.getEmail() : "N/A");
                    table.addCell(user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A");
                    table.addCell(user.getRole());
                    table.addCell(user.getDeletedAt() == null ? "Active" : "Deleted");
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

    private void deleteUser() throws SQLException {
        System.out.print("Enter user ID to delete: ");
        String userIdStr = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(userIdStr);

            // Prevent admin from deleting themselves
            if (userId == loggedInAdmin.getId()) {
                UiUtils.printError("You cannot delete your own account.");
                return;
            }

            // Confirm deletion
            System.out.print("Are you sure you want to delete this user? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("y")) {
                UiUtils.printMessage("Deletion cancelled.");
                return;
            }

            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                UiUtils.printSuccess("User deleted successfully.");
            } else {
                UiUtils.printError("Failed to delete user. User may not exist.");
            }

        } catch (NumberFormatException e) {
            UiUtils.printError("Invalid user ID format.");
        }
    }

    // ==================== BOOKING MANAGEMENT ====================
    private void bookingManagement() throws SQLException {
        while (true) {
            UiUtils.printHeader("BOOKING MANAGEMENT");

            UiUtils.printMenu(null,
                    "1. View all bookings",
                    "2. View pending bookings",
                    "3. Approve/Reject booking",
                    "4. Back to admin menu");

            System.out.print("Select an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllBookings();
                    break;
                case "2":
                    viewPendingBookings();
                    break;
                case "3":
                    approveOrRejectBooking();
                    break;
                case "4":
                    return;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    private void viewAllBookings() throws SQLException {
        List<Booking> bookings = bookingService.getAllBookings();

        UiUtils.printHeader("ALL BOOKINGS");

        if (bookings.isEmpty()) {
            UiUtils.printMessage("No bookings found.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Table table = UiUtils.createDataTable(7);
        table.addCell("ID");
        table.addCell("User");
        table.addCell("Room");
        table.addCell("Check-in");
        table.addCell("Check-out");
        table.addCell("Total");
        table.addCell("Status");

        for (Booking booking : bookings) {
            table.addCell(String.valueOf(booking.getId()));
            table.addCell(booking.getUsername());
            table.addCell(booking.getRoomNumber());
            table.addCell(booking.getCheckInDate().toString());
            table.addCell(booking.getCheckOutDate().toString());
            table.addCell("$" + booking.getTotalPrice());
            table.addCell(booking.getStatus().name());
        }
        System.out.println(table.render());

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void viewPendingBookings() throws SQLException {
        List<Booking> pendingBookings = bookingService.getPendingBookings();

        UiUtils.printHeader("PENDING BOOKINGS");

        if (pendingBookings.isEmpty()) {
            UiUtils.printMessage("No pending bookings found.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Table table = UiUtils.createDataTable(7);
        table.addCell("ID");
        table.addCell("User");
        table.addCell("Room");
        table.addCell("Check-in");
        table.addCell("Check-out");
        table.addCell("Total");
        table.addCell("Status");

        for (Booking booking : pendingBookings) {
            table.addCell(String.valueOf(booking.getId()));
            table.addCell(booking.getUsername());
            table.addCell(booking.getRoomNumber());
            table.addCell(booking.getCheckInDate().toString());
            table.addCell(booking.getCheckOutDate().toString());
            table.addCell("$" + booking.getTotalPrice());
            table.addCell(booking.getStatus().name());
        }
        System.out.println(table.render());

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void approveOrRejectBooking() throws SQLException {
        List<Booking> pendingBookings = bookingService.getPendingBookings();

        UiUtils.printHeader("APPROVE/REJECT BOOKING");

        if (pendingBookings.isEmpty()) {
            UiUtils.printMessage("No pending bookings to process.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Show pending bookings
        Table table = UiUtils.createDataTable(7);
        table.addCell("ID");
        table.addCell("User");
        table.addCell("Room");
        table.addCell("Check-in");
        table.addCell("Check-out");
        table.addCell("Total");
        table.addCell("Status");

        for (Booking booking : pendingBookings) {
            table.addCell(String.valueOf(booking.getId()));
            table.addCell(booking.getUsername());
            table.addCell(booking.getRoomNumber());
            table.addCell(booking.getCheckInDate().toString());
            table.addCell(booking.getCheckOutDate().toString());
            table.addCell("$" + booking.getTotalPrice());
            table.addCell(booking.getStatus().name());
        }
        System.out.println(table.render());

        // Get booking ID
        System.out.print("\nEnter Booking ID to process: ");
        String bookingIdStr = scanner.nextLine().trim();

        try {
            int bookingId = Integer.parseInt(bookingIdStr);
            Booking booking = pendingBookings.stream()
                    .filter(b -> b.getId() == bookingId)
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                UiUtils.printError("Pending booking not found with that ID.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }

            // Show booking details
            Table detailTable = UiUtils.createDataTable(2);
            detailTable.addCell("Booking ID:");
            detailTable.addCell(String.valueOf(booking.getId()));
            detailTable.addCell("User:");
            detailTable.addCell(booking.getUsername() + " (" + booking.getUserEmail() + ")");
            detailTable.addCell("Room:");
            detailTable.addCell(booking.getRoomNumber() + " (" + booking.getRoomTypeName() + ")");
            detailTable.addCell("Check-in:");
            detailTable.addCell(booking.getCheckInDate().toString());
            detailTable.addCell("Check-out:");
            detailTable.addCell(booking.getCheckOutDate().toString());
            detailTable.addCell("Total:");
            detailTable.addCell("$" + booking.getTotalPrice());
            System.out.println(detailTable.render());

            // Approve or reject
            System.out.print("Approve (a) or Reject (r)? ");
            String action = scanner.nextLine().trim().toLowerCase();

            if (action.equals("a")) {
                boolean approved = bookingService.approveBooking(bookingId);
                if (approved) {
                    UiUtils.printSuccess("BOOKING APPROVED!");
                } else {
                    UiUtils.printError("Failed to approve booking.");
                }
            } else if (action.equals("r")) {
                // Confirm rejection
                System.out.print("Are you sure you want to reject this booking? (y/n): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("y")) {
                    boolean rejected = bookingService.rejectBooking(bookingId);
                    if (rejected) {
                        UiUtils.printMessage("Booking rejected.");
                    } else {
                        UiUtils.printError("Failed to reject booking.");
                    }
                } else {
                    UiUtils.printMessage("Rejection cancelled.");
                }
            } else {
                UiUtils.printError("Invalid action. Please enter 'a' or 'r'.");
            }

        } catch (NumberFormatException e) {
            UiUtils.printError("Invalid booking ID format.");
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
