package view;

import model.entities.User;
import model.service.RoomService;
import model.entities.Room;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UserPanel {
    private User loggedInUser;
    private RoomService roomService;
    private Scanner scanner;

    public UserPanel(User user) {
        this.loggedInUser = user;
        this.roomService = new RoomService();
        this.scanner = new Scanner(System.in);
    }

    // Entry point – directly enter room listing
    public void startRoomListing() throws SQLException {
        viewAvailableRooms();  // will return here when user chooses to go back
    }

    private void viewAvailableRooms() throws SQLException {
        while (true) {
            Table headerTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
            headerTable.addCell("VIEW ROOMS");
            System.out.println(headerTable.render());

            Table viewTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER);
            viewTable.addCell("1. View all rooms (with features)");
            viewTable.addCell("2. Filter by room type");
            viewTable.addCell("3. Filter by availability status");
            viewTable.addCell("4. Filter by type AND status");
            viewTable.addCell("5. View detailed room features");
            viewTable.addCell("6. Back to login");
            System.out.println(viewTable.render());

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
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
            }
        }
    }

    // All remaining methods unchanged (they only use RoomService)
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
            headerTable.addCell("AVAILABLE ROOMS (Page " + pageNumber + "/" + totalPages + ")");
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
}