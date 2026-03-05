package controller;

import model.entities.User;
import model.service.UserService;
import view.UiUtils;
import view.UserPanel;

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
            UiUtils.printHeader("HOTEL RESERVATION SYSTEM");
            UiUtils.printMenu(null,
                    "1. Login",
                    "2. Register",
                    "3. Exit");

            System.out.print("Select an option (1-3): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    UiUtils.printMessage("Thank you for using our system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    private void handleRegister() {
        while (true) {
            UiUtils.printHeader("USER REGISTRATION");
            System.out.println("(Leave any field empty to cancel)");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) return;

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) return;

            System.out.print("Phone Number: ");
            String phoneNumber = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) return;

            try {
                boolean success = userService.register(username, email, phoneNumber, password);
                if (success) {
                    UiUtils.printSuccess("Registration successful! You can now login.");
                    return;
                } else {
                    UiUtils.printError("Registration failed. Please try again.");
                }
            } catch (Exception e) {
                UiUtils.printError("Registration error: " + e.getMessage());
                System.out.println("Please try again with valid information.");
            }
        }
    }

    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    private void handleLogin() throws SQLException {
        UiUtils.printHeader("LOGIN (Attempt " + (loginAttempts + 1) + " of " + MAX_ATTEMPTS + ")");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = userService.login(username, password);
            if (user != null && (user.getRole().equals("USER") || user.getRole().equals("ADMIN"))) {
                UiUtils.printSuccess("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                loginAttempts = 0; // Reset on success

                if (user.getRole().equals("ADMIN")) {
                    // Enter admin panel
                    view.AdminPanel adminPanel = new view.AdminPanel(user);
                    adminPanel.start();
                } else {
                    // Enter user panel with booking features
                    UserPanel userPanel = new UserPanel(user);
                    userPanel.start();
                }

                // After returning, back to login menu
            } else {
                loginAttempts++;
                UiUtils.printError("Invalid username or password.");

                if (loginAttempts >= MAX_ATTEMPTS) {
                    UiUtils.printError("Too many failed attempts. The program will now turn off.");
                    System.exit(0);
                } else {
                    System.out.println("You have " + (MAX_ATTEMPTS - loginAttempts) + " attempts remaining.");
                }
            }
        } catch (SQLException e) {
            UiUtils.printError("Database error: " + e.getMessage());
        } catch (Exception e) {
            UiUtils.printError("Login error: " + e.getMessage());
        }
    }
}