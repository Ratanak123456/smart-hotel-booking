package controller;

import model.entities.User;
import model.service.UserService;
import org.nocrala.tools.texttablefmt.BorderStyle;
import view.UserPanel;
import org.nocrala.tools.texttablefmt.Table;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthController {
    private UserService userService;
    private Scanner scanner;

    public AuthController() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public void showLoginMenu() throws SQLException {
        while (true) {
            Table headerTable = new Table(1 , BorderStyle.UNICODE_BOX_HEAVY_BORDER);
            headerTable.addCell("HOTEL RESERVATION SYSTEM");
            System.out.println(headerTable.render());

            Table menuTable = new Table(1 , BorderStyle.UNICODE_BOX);
            menuTable.addCell("1. Login");
            menuTable.addCell("2. Exit");
            System.out.println(menuTable.render());

            System.out.print("Select an option (1-2): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    Table footerTable = new Table(1);
                    footerTable.addCell("Thank you for using our system. Goodbye!");
                    System.out.println(footerTable.render());
                    System.exit(0);
                    break;
                default:
                    Table errorTable = new Table(1);
                    errorTable.addCell("Invalid option. Please try again.");
                    System.out.println(errorTable.render());
            }
        }
    }

    private void handleLogin() throws SQLException {
        Table headerTable = new Table(1);
        headerTable.addCell("LOGIN");
        System.out.println(headerTable.render());

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = userService.login(username, password);
            if (user != null && (user.getRole().equals("USER") || user.getRole().equals("ADMIN"))) {
                Table successTable = new Table(1);
                successTable.addCell("✓ Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                System.out.println(successTable.render());

                if (user.getRole().equals("ADMIN")) {
                    // Enter admin panel
                    view.AdminPanel adminPanel = new view.AdminPanel(user);
                    adminPanel.start();
                } else {
                    // Enter room listing panel
                    UserPanel userPanel = new UserPanel(user);
                    userPanel.startRoomListing();
                }

                // After returning, back to login menu
                return;
            } else {
                Table errorTable = new Table(1);
                errorTable.addCell("✗ Invalid username or password. Please try again.");
                System.out.println(errorTable.render());
            }
        } catch (SQLException e) {
            Table errorTable = new Table(1);
            errorTable.addCell("✗ Database error: " + e.getMessage());
            System.out.println(errorTable.render());
        } catch (Exception e) {
            Table errorTable = new Table(1);
            errorTable.addCell("✗ Login error: " + e.getMessage());
            System.out.println(errorTable.render());
        }
    }
}