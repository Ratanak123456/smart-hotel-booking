package view;

import model.entities.User;
import model.entities.Room;
import model.entities.Booking;
import model.service.RoomService;
import model.service.UserService;
import model.service.BookingService;
import org.nocrala.tools.texttablefmt.BorderStyle;
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
            Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
            headerTable.addCell("ADMIN PANEL - Welcome, " + loggedInAdmin.getUsername());
            System.out.println(headerTable.render());

            Table menuTable = new Table(1, BorderStyle.UNICODE_BOX);
            menuTable.addCell("1. Room Management");
            menuTable.addCell("2. User Management");
            menuTable.addCell("3. Booking Management");
            menuTable.addCell("4. Logout");
            System.out.println(menuTable.render());

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
                    Table logoutTable = new Table(1);
                    logoutTable.addCell("Logged out successfully.");
                    System.out.println(logoutTable.render());
                    return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
            }
        }
    }

    // ==================== ROOM MANAGEMENT ====================
    private void roomManagement() throws SQLException {
        while (true) {
            Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
            headerTable.addCell("ROOM MANAGEMENT");
            System.out.println(headerTable.render());

            Table menuTable = new Table(1, BorderStyle.UNICODE_BOX);
            menuTable.addCell("1. View all rooms");
            menuTable.addCell("2. Search/Filter by room type");
            menuTable.addCell("3. Search/Filter by availability status");
            menuTable.addCell("4. Filter by type AND status");
            menuTable.addCell("5. Sort rooms by price");
            menuTable.addCell("6. View detailed room features");
            menuTable.addCell("7. Back to admin menu");
            System.out.println(menuTable.render());

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
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void filterByRoomType() throws SQLException {
        Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
        headerTable.addCell("Available Room Types");
        System.out.println(headerTable.render());

        Table typeTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER);
        typeTable.addCell("1. Regular");
        typeTable.addCell("2. Family");
        typeTable.addCell("3. Suite");
        typeTable.addCell("4. Deluxe");
        System.out.println(typeTable.render());

        System.out.print("Select room type (1-4): ");
        String choice = scanner.nextLine().trim();
        String roomType = null;
        switch (choice) {
            case "1": roomType = "Regular"; break;
            case "2": roomType = "Family"; break;
            case "3": roomType = "Suite"; break;
            case "4": roomType = "Deluxe"; break;
            default:
                Table errorTable = new Table(1);
                errorTable.addCell("Invalid option.");
                System.out.println(errorTable.render());
                return;
        }
        displayRoomsPaginated(roomType, null);
    }

    private void filterByRoomStatus() throws SQLException {
        Table headerTable = new Table(1);
        headerTable.addCell("Room Status Options");
        System.out.println(headerTable.render());

        Table statusTable = new Table(1);
        statusTable.addCell("1. Available");
        statusTable.addCell("2. Occupied");
        statusTable.addCell("3. Maintenance");
        System.out.println(statusTable.render());

        System.out.print("Select status (1-3): ");
        String choice = scanner.nextLine().trim();
        String status = null;
        switch (choice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "OCCUPIED"; break;
            case "3": status = "MAINTENANCE"; break;
            default:
                Table errorTable = new Table(1);
                errorTable.addCell("Invalid option.");
                System.out.println(errorTable.render());
                return;
        }
        displayRoomsPaginated(null, status);
    }

    private void filterByTypeAndStatus() throws SQLException {
        Table typeHeaderTable = new Table(1);
        typeHeaderTable.addCell("Select Room Type");
        System.out.println(typeHeaderTable.render());

        Table typeTable = new Table(1);
        typeTable.addCell("1. Regular");
        typeTable.addCell("2. Family");
        typeTable.addCell("3. Suite");
        typeTable.addCell("4. Deluxe");
        System.out.println(typeTable.render());

        System.out.print("Select room type (1-4): ");
        String typeChoice = scanner.nextLine().trim();
        String roomType = null;
        switch (typeChoice) {
            case "1": roomType = "Regular"; break;
            case "2": roomType = "Family"; break;
            case "3": roomType = "Suite"; break;
            case "4": roomType = "Deluxe"; break;
            default:
                Table errorTable = new Table(1);
                errorTable.addCell("Invalid option.");
                System.out.println(errorTable.render());
                return;
        }

        Table statusHeaderTable = new Table(1);
        statusHeaderTable.addCell("Select Room Status");
        System.out.println(statusHeaderTable.render());

        Table statusTable = new Table(1);
        statusTable.addCell("1. Available");
        statusTable.addCell("2. Occupied");
        statusTable.addCell("3. Maintenance");
        System.out.println(statusTable.render());

        System.out.print("Select status (1-3): ");
        String statusChoice = scanner.nextLine().trim();
        String status = null;
        switch (statusChoice) {
            case "1": status = "AVAILABLE"; break;
            case "2": status = "OCCUPIED"; break;
            case "3": status = "MAINTENANCE"; break;
            default:
                Table errorTable = new Table(1);
                errorTable.addCell("Invalid option.");
                System.out.println(errorTable.render());
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

            Table headerTable = new Table(1);
            headerTable.addCell("ROOMS (Page " + pageNumber + "/" + totalPages + ")");
            System.out.println(headerTable.render());

            if (rooms.isEmpty()) {
                Table emptyTable = new Table(1);
                emptyTable.addCell("No rooms found.");
                System.out.println(emptyTable.render());
            } else {
                Table table = new Table(6);
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

            Table navTable = new Table(1);
            if (pageNumber > 1) navTable.addCell("p - Previous page");
            if (pageNumber < totalPages) navTable.addCell("n - Next page");
            navTable.addCell("b - Back");
            System.out.println(navTable.render());

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p":
                    if (pageNumber > 1) pageNumber--;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on first page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "n":
                    if (pageNumber < totalPages) pageNumber++;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on last page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "b":
                    return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option.");
                    System.out.println(errorTable.render());
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

            Table headerTable = new Table(1);
            headerTable.addCell("FILTERED ROOMS - " + roomType + " - " + status + " (Page " + pageNumber + "/" + totalPages + ")");
            System.out.println(headerTable.render());

            if (rooms.isEmpty()) {
                Table emptyTable = new Table(1);
                emptyTable.addCell("No rooms found matching your criteria.");
                System.out.println(emptyTable.render());
            } else {
                Table table = new Table(6);
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

            Table navTable = new Table(1);
            if (pageNumber > 1) navTable.addCell("p - Previous page");
            if (pageNumber < totalPages) navTable.addCell("n - Next page");
            navTable.addCell("b - Back");
            System.out.println(navTable.render());

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p": if (pageNumber > 1) pageNumber--; break;
                case "n": if (pageNumber < totalPages) pageNumber++; break;
                case "b": return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void sortRoomsByPrice() throws SQLException {
        Table sortTable = new Table(1);
        sortTable.addCell("1. Sort by price (Low to High)");
        sortTable.addCell("2. Sort by price (High to Low)");
        System.out.println(sortTable.render());

        System.out.print("Select sort option (1-2): ");
        String choice = scanner.nextLine().trim();

        final boolean ascending;
        if (choice.equals("2")) {
            ascending = false;
        } else if (!choice.equals("1")) {
            Table errorTable = new Table(1);
            errorTable.addCell("Invalid option.");
            System.out.println(errorTable.render());
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
            Table headerTable = new Table(1);
            headerTable.addCell("ROOMS SORTED BY PRICE (" + sortLabel + ") - Page " + pageNumber + "/" + totalPages);
            System.out.println(headerTable.render());

            if (rooms.isEmpty()) {
                Table emptyTable = new Table(1);
                emptyTable.addCell("No rooms found.");
                System.out.println(emptyTable.render());
            } else {
                Table table = new Table(6);
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

            Table navTable = new Table(1);
            if (pageNumber > 1) navTable.addCell("p - Previous page");
            if (pageNumber < totalPages) navTable.addCell("n - Next page");
            navTable.addCell("b - Back");
            System.out.println(navTable.render());

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p": if (pageNumber > 1) pageNumber--; break;
                case "n": if (pageNumber < totalPages) pageNumber++; break;
                case "b": return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void viewDetailedRoomFeatures() throws SQLException {
        Table headerTable = new Table(1);
        headerTable.addCell("ROOM DETAILS WITH COMPLETE FEATURES");
        System.out.println(headerTable.render());

        List<Room> rooms = roomService.getAllRoomsPaginated(1);
        Table listTable = new Table(5);
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
                Table errorTable = new Table(1);
                errorTable.addCell("Room not found.");
                System.out.println(errorTable.render());
                return;
            }

            Table detailHeader = new Table(1);
            detailHeader.addCell("COMPLETE ROOM DETAILS");
            System.out.println(detailHeader.render());

            Table detailTable = new Table(2);
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
            Table errorTable = new Table(1);
            errorTable.addCell("Invalid room ID format.");
            System.out.println(errorTable.render());
        }
    }

    // ==================== USER MANAGEMENT ====================
    private void userManagement() throws SQLException {
        while (true) {
            Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
            headerTable.addCell("USER MANAGEMENT");
            System.out.println(headerTable.render());

            Table menuTable = new Table(1, BorderStyle.UNICODE_BOX);
            menuTable.addCell("1. List all users");
            menuTable.addCell("2. Search user by username");
            menuTable.addCell("3. Delete user");
            menuTable.addCell("4. Back to admin menu");
            System.out.println(menuTable.render());

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
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
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

            Table headerTable = new Table(1);
            headerTable.addCell("ALL USERS (Page " + pageNumber + "/" + totalPages + ")");
            System.out.println(headerTable.render());

            if (users.isEmpty()) {
                Table emptyTable = new Table(1);
                emptyTable.addCell("No users found.");
                System.out.println(emptyTable.render());
            } else {
                Table table = new Table(6);
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

            Table navTable = new Table(1);
            if (pageNumber > 1) navTable.addCell("p - Previous page");
            if (pageNumber < totalPages) navTable.addCell("n - Next page");
            navTable.addCell("b - Back");
            System.out.println(navTable.render());

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p":
                    if (pageNumber > 1) pageNumber--;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on first page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "n":
                    if (pageNumber < totalPages) pageNumber++;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on last page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "b":
                    return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void searchUser() throws SQLException {
        System.out.print("Enter username to search: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            Table errorTable = new Table(1);
            errorTable.addCell("Search term cannot be empty.");
            System.out.println(errorTable.render());
            return;
        }

        int pageNumber = 1;
        int rowsPerPage = 5;

        while (true) {
            List<User> users = userService.searchUsersByUsername(searchTerm, pageNumber);
            int totalCount = userService.getSearchUsersCount(searchTerm);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / rowsPerPage));

            Table headerTable = new Table(1);
            headerTable.addCell("SEARCH RESULTS FOR '" + searchTerm + "' (Page " + pageNumber + "/" + totalPages + ")");
            System.out.println(headerTable.render());

            if (users.isEmpty()) {
                Table emptyTable = new Table(1);
                emptyTable.addCell("No users found matching '" + searchTerm + "'.");
                System.out.println(emptyTable.render());
            } else {
                Table table = new Table(6);
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

            Table navTable = new Table(1);
            if (pageNumber > 1) navTable.addCell("p - Previous page");
            if (pageNumber < totalPages) navTable.addCell("n - Next page");
            navTable.addCell("b - Back");
            System.out.println(navTable.render());

            System.out.print("Choose: ");
            String navChoice = scanner.nextLine().trim().toLowerCase();

            switch (navChoice) {
                case "p":
                    if (pageNumber > 1) pageNumber--;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on first page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "n":
                    if (pageNumber < totalPages) pageNumber++;
                    else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Already on last page.");
                        System.out.println(errorTable.render());
                    }
                    break;
                case "b":
                    return;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option.");
                    System.out.println(errorTable.render());
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
                Table errorTable = new Table(1);
                errorTable.addCell("You cannot delete your own account.");
                System.out.println(errorTable.render());
                return;
            }

            // Confirm deletion
            System.out.print("Are you sure you want to delete this user? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("y")) {
                Table cancelTable = new Table(1);
                cancelTable.addCell("Deletion cancelled.");
                System.out.println(cancelTable.render());
                return;
            }

            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                Table successTable = new Table(1);
                successTable.addCell("✓ User deleted successfully.");
                System.out.println(successTable.render());
            } else {
                Table errorTable = new Table(1);
                errorTable.addCell("Failed to delete user. User may not exist.");
                System.out.println(errorTable.render());
            }

        } catch (NumberFormatException e) {
            Table errorTable = new Table(1);
            errorTable.addCell("Invalid user ID format.");
            System.out.println(errorTable.render());
        }
    }

    // ==================== BOOKING MANAGEMENT ====================
    private void bookingManagement() throws SQLException {
        while (true) {
            Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
            headerTable.addCell("BOOKING MANAGEMENT");
            System.out.println(headerTable.render());

            Table menuTable = new Table(1, BorderStyle.UNICODE_BOX);
            menuTable.addCell("1. View all bookings");
            menuTable.addCell("2. View pending bookings");
            menuTable.addCell("3. Approve/Reject booking");
            menuTable.addCell("4. Back to admin menu");
            System.out.println(menuTable.render());

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
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void viewAllBookings() throws SQLException {
        List<Booking> bookings = bookingService.getAllBookings();

        Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
        headerTable.addCell("ALL BOOKINGS");
        System.out.println(headerTable.render());

        if (bookings.isEmpty()) {
            Table emptyTable = new Table(1);
            emptyTable.addCell("No bookings found.");
            System.out.println(emptyTable.render());
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Table table = new Table(7);
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

        Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
        headerTable.addCell("PENDING BOOKINGS");
        System.out.println(headerTable.render());

        if (pendingBookings.isEmpty()) {
            Table emptyTable = new Table(1);
            emptyTable.addCell("No pending bookings found.");
            System.out.println(emptyTable.render());
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Table table = new Table(7);
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

        Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
        headerTable.addCell("APPROVE/REJECT BOOKING");
        System.out.println(headerTable.render());

        if (pendingBookings.isEmpty()) {
            Table emptyTable = new Table(1);
            emptyTable.addCell("No pending bookings to process.");
            System.out.println(emptyTable.render());
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Show pending bookings
        Table table = new Table(7);
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
                Table errorTable = new Table(1);
                errorTable.addCell("Pending booking not found with that ID.");
                System.out.println(errorTable.render());
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }

            // Show booking details
            Table detailTable = new Table(2);
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
                    Table successTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
                    successTable.addCell("BOOKING APPROVED!");
                    System.out.println(successTable.render());
                } else {
                    Table errorTable = new Table(1);
                    errorTable.addCell("Failed to approve booking.");
                    System.out.println(errorTable.render());
                }
            } else if (action.equals("r")) {
                // Confirm rejection
                System.out.print("Are you sure you want to reject this booking? (y/n): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("y")) {
                    boolean rejected = bookingService.rejectBooking(bookingId);
                    if (rejected) {
                        Table successTable = new Table(1);
                        successTable.addCell("Booking rejected.");
                        System.out.println(successTable.render());
                    } else {
                        Table errorTable = new Table(1);
                        errorTable.addCell("Failed to reject booking.");
                        System.out.println(errorTable.render());
                    }
                } else {
                    Table cancelTable = new Table(1);
                    cancelTable.addCell("Rejection cancelled.");
                    System.out.println(cancelTable.render());
                }
            } else {
                Table errorTable = new Table(1);
                errorTable.addCell("Invalid action. Please enter 'a' or 'r'.");
                System.out.println(errorTable.render());
            }

        } catch (NumberFormatException e) {
            Table errorTable = new Table(1);
            errorTable.addCell("Invalid booking ID format.");
            System.out.println(errorTable.render());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
